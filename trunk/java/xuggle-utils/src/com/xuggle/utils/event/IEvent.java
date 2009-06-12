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
   * Called internally by dispatchers right before a {@link IEventDispatcher#dispatchEvent(IEvent)}
   * is attempted.
   * @param dispatcher the dispatcher added to.
   * @return should return the total number of {@link #preDispatch(IEventDispatcher)}
   *   calls - the total number of {@link #postHandle(IEventDispatcher)} calls.
   */
  long preDispatch(IEventDispatcher dispatcher);
  
  /**
   * Called by the {@link IEventDispatcher} when all handlers
   * for this event have been called during the dispatch cycle.
   * If this method returns 0, the {@link IEventDispatcher} will
   * call {@link #delete()} on this event.
   * @param dispatcher
   * @return should return the total number of {@link #preDispatch(IEventDispatcher)}
   *   calls - the total number of {@link #postHandle(IEventDispatcher)} calls.
   */
  long postHandle(IEventDispatcher dispatcher);
  
  /**
   * A method that is called when all dispatched events have finished
   * being handled.
   */
  void delete();

}
