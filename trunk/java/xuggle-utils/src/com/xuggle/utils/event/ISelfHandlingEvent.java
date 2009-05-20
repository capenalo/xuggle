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

package com.xuggle.utils.event;

/**
 * If you implement this interface, then an event dispatcher will
 * auto-handle this event when it comes time to dispatch.  This is
 * a handy way to register an event but not worry about having a
 * a listener.
 * 
 * <p>
 * 
 * This handleEvent() gets called before any other handleEvents().
 * 
 * </p>
 * 
 * @author aclarke
 *
 */

public interface ISelfHandlingEvent<E extends IEvent>
extends IEvent, IEventHandler<E>
{
}
