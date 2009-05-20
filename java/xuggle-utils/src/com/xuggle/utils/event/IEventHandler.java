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
 * Handler for events.
 * <p>
 * Classes that implement this interface can be added to an
 * IEventDispatcher.  
 * </p>
 * <p>
 * The EventDispatcher will then call any handlers when an event is
 * dispatched, but will stop the execution of handlers once one
 * handler returns true.
 * </p>
 * @author aclarke
 *
 */
public interface IEventHandler<E extends IEvent>
{

  /**
   * Any applicable events are passed to this
   * method for handling.
   * <p>
   * If this method throws any uncaught exceptions, then the
   * calling {@link IEventDispatcher} will dispatch an {@link ErrorEvent}
   * but will not re-throw the exception.
   * </p>
   * 
   * @param dispatcher The dispatcher for this event
   * @param event The event this handler can handle
   * @return true means the event was handled, and it should not be passed to
   *   any other handler.
   */
  boolean handleEvent(IEventDispatcher dispatcher, E event);
}
