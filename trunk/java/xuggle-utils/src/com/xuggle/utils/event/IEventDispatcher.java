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
 * An interface that can have events dispatched to it.
 * 
 * <p>
 * 
 * The paradigm for dispatching events is:
 * 
 * </p>
 * 
 * <ol>
 * 
 * <li>Find all appropriate handlers, and call them in order
 * of decreasing priority, stopping when a handler has
 * successfully handled the event.</li>
 * <li>If two handlers are registered at the same priority,
 * their order of execution is undetermined.</li>
 * <li>If any handler throws an uncaught Exception, {@link IEventDispatcher}
 * objects will dispatch an {@link ErrorEvent} and continue with the next
 * handler</li>
 * <li>If the thread a dispatcher is running on is interrupted,
 * {@link IEventDispatcher} objects will return in a reasonable amount of time.</li>
 * <li>Dispatchers must ensure that no handlers registered on that dispatcher
 * may be called concurrently, even though {@link #dispatchEvent(IEvent)} may be
 * called concurrently.
 * 
 * </li>
 * 
 * </ol>
 * 
 * @author aclarke
 * 
 */
public interface IEventDispatcher extends IEventHandlerRegistrable
{

  /**
   * Takes the given event, and passes it to all appropriate handlers.
   * 
   * @param event The event to dispatch
   */
  void dispatchEvent(IEvent event);

}
