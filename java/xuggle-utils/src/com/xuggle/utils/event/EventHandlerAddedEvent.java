/*******************************************************************************
 * Copyright (c) 2008, 2010 Xuggle Inc.  All rights reserved.
 *  
 * This file is part of Xuggle-Utils.
 *
 * Xuggle-Utils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Xuggle-Utils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Xuggle-Utils.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package com.xuggle.utils.event;

/**
 * Fired by an {@link IEventDispatcher} when an {@link IEventHandler}
 * object is added.
 *  
 * @author aclarke
 *
 */

public class EventHandlerAddedEvent extends Event
{

  private final IEventHandlerRegistrable.Key mKey;
  private final int mPriority;
  private final Class<? extends IEvent> mEventClass;
  private final IEventHandler<? extends IEvent> mHandler;
  private final boolean mKeepingWeakReference;

  /**
   * Fired when an {@link IEventDispatcher} object adds an event handler.
   * 
   * Only for creation by EventDispatchers.
   *  
   * @param source The {@link IEventDispatcher} the handler was added to
   * @param key The key that was returned by the addEventHandler method.
   * @param priority The priority used (higher means higher priority)
   * @param eventClass The class that the handler will be executed for.
   * @param handler The handler itself
   * @param isKeepingWeekReference True if the {@link IEventDispatcher} is
   *   only keeping a weak reference to this object.
   */
  
  public EventHandlerAddedEvent(IEventDispatcher source,
      IEventHandlerRegistrable.Key key,
      int priority,
      Class<? extends IEvent> eventClass,
      IEventHandler<? extends IEvent> handler,
      boolean isKeepingWeekReference)
  {
    super(source);
    mPriority = priority;
    mEventClass = eventClass;
    mHandler = handler;
    mKeepingWeakReference = isKeepingWeekReference;
    mKey = key;
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
   * Is the {@link IEventDispatcher} keeping a weak reference to this object
   *  
   * @return true if the {@link IEventDispatcher} only keeps a weak reference
   */
  
  public boolean isKeepingWeakReference()
  {
    return mKeepingWeakReference;
  }

  /**
   * The handler registered
   * @return The handler
   */
  
  public IEventHandler<? extends IEvent> getHandler()
  {
    return mHandler;
  }

  /**
   * The event class that the handler will be called for.
   *  
   * @return the event class.
   */
  
  public Class<? extends IEvent> getEventClass()
  {
    return mEventClass;
  }

  /**
   * The priority that the handler will be called at.
   *  
   * @return the priority.
   */
  
  public int getPriority()
  {
    return mPriority;
  }

  /**
   * The key for use in {@link IEventHandlerRegistrable#removeEventHandler(IEventHandlerRegistrable.Key)}
   * @return the key
   */
  public IEventHandlerRegistrable.Key getKey()
  {
    return mKey;
  }
  

}
