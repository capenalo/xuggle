/*
 * This file is part of Xuggler.
 * 
 * Xuggler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Xuggler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Xuggler.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xuggle.utils.event.handler;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.xuggle.utils.event.IEvent;
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.IEventHandler;
import com.xuggle.utils.event.IEventHandlerRegistrable;

/**
 * An {@link IEventHandler} that proxies another {@link IEventHandler},
 * but only executes the proxied handler a maximum
 * number of times.
 * <p>
 * The user is still responsible for calling
 *  {@link IEventHandlerRegistrable#removeEventHandler(int, Class, IEventHandler)}
 *  if appropriate for this handler to avoid clogging up memory in a
 *  dispatcher, but after the maximum number of calls have been reached,
 *  this handler will not handle any more events.  Note that if you don't
 *  remove this handler, it will still expend CPU cycles being matched
 *  and detecting NOT to fire.
 *</p>
 * @author aclarke
 *
 * @param <E> The underlying IEvent type you want to handle
 */
public class BoundedHandler<E extends IEvent>
  implements IEventHandler<E>
{
  /** This is used as a sentinel value to signify no
   * call back handler.
   */
  private final IEventHandler<E> mNullCallbackHandler
    = new IEventHandler<E>()
    {
      public boolean handleEvent(IEventDispatcher dispatcher, E event)
      {
        return false;
      }
    };
  private final AtomicInteger mExecCount;
  private final int mMaxExecCount;
  private final IEventHandler<E> mEventHandler;
  
  private final AtomicReference<IEventHandler<E>> mLastCallHandler;
  
  /**
   * Creates a new object.
   * @param maxTimesToCallHandler Maximum number of times to call
   * @param proxiedHandler We will forward our
   *  {@link #handleEvent(IEventDispatcher, IEvent)} to the corresponding
   *  method on this handler.
   * @throws IllegalArgumentException if maxTimesToCallHandler <= 0
   * @throws IllegalArgumentException if proxiedHandler == null
   */
  protected BoundedHandler(
      final int maxTimesToCallHandler,
      final IEventHandler<E> proxiedHandler
      )
  {
    if (proxiedHandler == null)
      throw new IllegalArgumentException();
    if (maxTimesToCallHandler <= 0)
      throw new IllegalArgumentException();
    
    mMaxExecCount = maxTimesToCallHandler;
    mEventHandler = proxiedHandler;
    mExecCount = new AtomicInteger(0);
    mLastCallHandler = new AtomicReference<IEventHandler<E>>();
    mLastCallHandler.set(mNullCallbackHandler);
  }

  /**
   * Sets a handler that we will call AFTER the last actual
   * handleEvent has been called.  This will only ever be called
   * once, and will not be called if we've already passed the
   * maximum numbers of all.
   * <p>
   * Also, the first setter of this wins; all other sets
   * are ignored.
   * </p>
   *  
   * @param eventHandler if non null, we will call this back
   *  <strong>after</strong> we've been called the last time.
   *  Callers can use this handler to do things like remove
   *  the actual handler from the dispatcher.
   */
  public void setLastEventHandler(IEventHandler<E> eventHandler)
  {
    mLastCallHandler.compareAndSet(mNullCallbackHandler, eventHandler);
  }
  
  /**
   * Get the number of times this handler has executed the underlying
   * forwarded to handle.
   * <p>
   * Note that due to multi-threaded madness, this is only an
   * approximation and should only be used as a debugging aide.
   * </p>
   * @return The number of times executed
   */
  public int getNumTimesExecuted()
  {
    return mExecCount.get();
  }

  /**
   * Returns the {@link IEventHandler} this object calls
   * when its {@link #handleEvent(IEventDispatcher, IEvent)} method
   * is called.
   * @return the proxied event handler.
   */
  public IEventHandler<E> getProxiedHandler()
  {
    return mEventHandler;
  }
  
  public boolean handleEvent(IEventDispatcher dispatcher, E event)
  {
    int execCount = mExecCount.getAndIncrement();
    boolean retval = false;
    if (execCount < mMaxExecCount)
      retval = mEventHandler.handleEvent(dispatcher, event);
    // now this is odd, but if exec count ever exceeds the max
    // exec count, then we subtract one.  this stops us rounding
    // around if we get a lot of executions -- usually because
    // someone forgot to remove an event handler
    if (execCount >= mMaxExecCount)
    {
      // we set the handler to null when we retrieve it to make
      // sure that only one thread calls the callback!
      final IEventHandler<E> callback = mLastCallHandler.getAndSet(null);
      if (callback != null && callback != mNullCallbackHandler)
        callback.handleEvent(dispatcher, event);
      mExecCount.decrementAndGet();
    }
    return retval;
  }
  
}
