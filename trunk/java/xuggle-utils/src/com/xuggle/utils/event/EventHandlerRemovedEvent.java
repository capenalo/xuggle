/*
 * Copyright (c) 2008, 2009 by Xuggle Incorporated.  All rights reserved.
 * 
 * This file is part of Xuggler.
 * 
 * You can redistribute Xuggler and/or modify it under the terms of the GNU
 * Affero General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Xuggler is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with Xuggler.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xuggle.utils.event;

import java.lang.ref.ReferenceQueue;

/**
 * Fired by an {@link IEventDispatcher} when an {@link IEventHandler}
 * object is removed.
 * 
 * <p>
 * 
 * This is fired in response to a successful
 *  {@link IEventHandlerRegistrable#removeEventHandler(int, Class, IEventHandler)}
 * call.  It will also be fired if the {@link IEventDispatcher} is maintaining
 * weak references to a {@link IEventHandler}, but the {@link IEventDispatcher}
 * detects the weak reference has been collected.
 * 
 * </p><p>
 * 
 * Users cannot guarantee this event is fired for all {@link IEventHandler}s
 * the {@link IEventDispatcher} kept a weak reference to.  It will only get fired
 * if:
 * 
 * <ul>
 * 
 * <li>The Java Garbage Collector collects the underlying handler; <strong>
 * AND</strong></li>
 * 
 * <li>Java adds the weak reference the {@link IEventDispatcher} is keeping
 * to an internal {@link ReferenceQueue} maintained in the
 * {@link IEventDispatcher}; <strong>AND</strong></li>
 * 
 * <li>At least one more {@link IEventDispatcher#dispatchEvent(IEvent)} call
 * is made on the {@link IEventDispatcher} in question.</li>
 * 
 * </ul>
 * 
 * Depending upon memory needs and how you use events in your program only
 * some of those events will occur.  In general if you're using weak references
 * for handlers, you should never rely on this event being fired.  If you're
 * using strong references, you may rely on this event being fired in response
 * to
 * {@link IEventHandlerRegistrable#removeEventHandler(int, Class, IEventHandler)}
 * 
 * </p>
 * 
 *  
 * @author aclarke
 *
 */

public class EventHandlerRemovedEvent extends Event
{

  private final int mPriority;
  private final Class<? extends IEvent> mEventClass;
  private final IEventHandler<? extends IEvent> mHandler;

  /**
   * Fired when an {@link IEventDispatcher} object
   * removes an event handler.
   * 
   * Only for creation by EventDispatchers.
   *  
   * @param source The {@link IEventDispatcher} the handler was removed from
   * @param priority The priority used (higher means higher priority)
   * @param eventClass The class that the handler would have been executed for.
   * @param handler The handler itself, or null if unknown.
   */
  
  public EventHandlerRemovedEvent(IEventDispatcher source,
      int priority,
      Class<? extends IEvent> eventClass,
      IEventHandler<? extends IEvent> handler)
  {
    super(source);
    mPriority = priority;
    mEventClass = eventClass;
    mHandler = handler;
  }
  
  /**
   * Get the source dispatcher for this event.
   * 
   *  @return the source dispatcher
   */
  
  @Override
  public IEventDispatcher getSource()
  {
    return (IEventDispatcher)super.getSource();
  }

  /**
   * The handler registered.
   * 
   * <p>
   * 
   * This can return null if the {@link IEventDispatcher} was keeping
   * a weak reference, and that week reference was collected.
   * 
   * </p>
   * 
   * @return The handler, or null if the originally registered handler was
   *   added with a weak reference which has been collected.
   */
  
  public IEventHandler<? extends IEvent> getHandler()
  {
    return mHandler;
  }

  /**
   * The event class that the handler would have been called for.
   *  
   * @return the event class.
   */
  
  public Class<? extends IEvent> getEventClass()
  {
    return mEventClass;
  }

  /**
   * The priority that the handler would have been called at.
   *  
   * @return the priority.
   */
  
  public int getPriority()
  {
    return mPriority;
  }
  

}
