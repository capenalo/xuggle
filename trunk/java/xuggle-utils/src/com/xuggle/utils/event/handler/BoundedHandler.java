package com.xuggle.utils.event.handler;

import java.util.concurrent.atomic.AtomicInteger;

import com.xuggle.utils.event.IEvent;
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.IEventHandler;
import com.xuggle.utils.event.IEventHandlerRegistrable;

public class BoundedHandler<E extends IEvent>
  implements IEventHandler<E>
{
  private final AtomicInteger mExecCount;
  private final int mMaxExecCount;
  private final IEventHandler<E> mEventHandler;
  
  protected BoundedHandler(
      final Class<E> eventClass,
      final IEventHandler<E> eventHandler,
      final int maxTimesToCallHandler
      )
  {
    this(null, 0, eventClass, eventHandler, maxTimesToCallHandler);
  }
  
  protected BoundedHandler(
      final IEventHandlerRegistrable registry,
      final Class<E> eventClass,
      final IEventHandler<E> eventHandler,
      final int maxTimesToCallHandler
      )
  {
    this(registry, 0, eventClass, eventHandler, maxTimesToCallHandler);
  }
  
  protected BoundedHandler(
      final IEventHandlerRegistrable registry,
      final int priority,
      final Class<E> eventClass,
      final IEventHandler<E> eventHandler,
      final int maxTimesToCallHandler
      )
  {
    if (eventHandler == null)
      throw new IllegalArgumentException();
    if (eventClass == null)
      throw new IllegalArgumentException();
    
    mMaxExecCount = maxTimesToCallHandler;
    mEventHandler = eventHandler;
    mExecCount = new AtomicInteger(0);
    if (mMaxExecCount > 0 && registry != null)
    {
      registry.addEventHandler(priority, eventClass,
          this);
    }
  }
  
  public boolean handleEvent(IEventDispatcher dispatcher, E event)
  {
    int execCount = mExecCount.incrementAndGet();
    boolean retval = false;
    try
    {
      if (this != mEventHandler)
        retval = mEventHandler.handleEvent(dispatcher, event);
    }
    finally
    {
      if (execCount >= mMaxExecCount)
      {
        // we've executed enough; time to remove ourselves
        dispatcher.removeEventHandler(0,
            event.getClass(),
            this);
      }
    }
    return retval;
  }
  
  public int getNumTimesExecuted()
  {
    return mExecCount.get();
  }

}
