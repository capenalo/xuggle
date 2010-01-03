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
 * This is the base Interface for ALL events in Xuggle.
 * 
 * @author aclarke
 *
 */
public interface IEvent
{

  /**
   * Get a reference to the Object that generated this
   * event.
   * 
   * It is recommended that subclasses Foo of IEvent override this
   * method to create a method that returns FooSourceType if
   * it's known that only FooSourceType can create a Foo event.  For
   * example:
   * <pre> 
   * FooSourceType getSource()
   * {
   *   return (FooSourceType) super.getSource();
   * }
   * </pre>
   * 
   * This helps avoid casting by callers.
   * 
   * @return The source of this event.
   */
  Object getSource();
  
  /**
   * When was this event created?
   * 
   * @return The time, as returned from {@link System#nanoTime()} when
   * this event was created.
   */
  long getWhen();

  /**
   * <p>
   * Call this from an {@link IEventHandler} if you need to ensure
   * the event remains valid after your handler returns.
   * </p>
   * <p>
   * When you call {@link IEventDispatcher#dispatchEvent(IEvent)} with
   * this {@link IEvent}, the dispatcher will call {@link #acquire()} before
   * processing the event, and {@link #release()} when done.  In this way
   * if you re-dispatch the event during handling, it won't be {@link #delete()}ed
   * until all dispatchers have finished.
   * </p>
   * <p>
   * Also, objects like {@link ErrorEvent} that contain other events,
   * will call {@link #acquire()} to keep the event, and {@link #release()}
   * when that event is actually {@link #delete()}ed.
   * </p>
   * 
   * @return should return the total number of {@link #acquire()}
   *   calls - the total number of {@link #release()} calls.
   */
  long acquire();
  
  /**
   * Called by the {@link IEventDispatcher} when all handlers
   * for this event have been called during the dispatch cycle.
   * If this method returns 0, the {@link IEvent} implementation should
   * call {@link #delete()} internally.
   * @return should return the total number of {@link #acquire()}
   *   calls - the total number of {@link #release()} calls.
   */
  long release();
  
  /**
   * A method that is called when all dispatched events have finished
   * being handled.
   * @throws IllegalStateException if the total number of references
   *   to this event are not null.
   */
  void delete();

}
