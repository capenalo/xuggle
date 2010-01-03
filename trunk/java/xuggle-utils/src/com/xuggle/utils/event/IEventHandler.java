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
