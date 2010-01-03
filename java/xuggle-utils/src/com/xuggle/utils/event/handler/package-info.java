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

/**
 * Provides convenience methods for registering, and
 * special implementations of,
 * {@link com.xuggle.utils.event.IEventHandler}.
 * <p>
 * There are certain types of {@link com.xuggle.utils.event.IEventHandler}
 * implementations that are very common.  For example, sometimes
 * you want to forward an event from one
 * {@link com.xuggle.utils.event.IEventDispatcher}
 * to another.
 * Sometimes you only want
 * a {@link com.xuggle.utils.event.IEventHandler} to execute if the
 * {@link com.xuggle.utils.event.IEvent#getSource()} is equal to
 * a given source.
 * Sometimes you only
 * want to handler to execute a maximum number of times.
 * </p>
 * <p>
 * This class tries to provide some of those implementations for you.
 * </p>
 * <p>
 * Use the {@link com.xuggle.utils.event.handler.Handler} class to find
 * Factory methods for the special handlers you want.
 * </p>
 * @see com.xuggle.utils.event.handler.Handler
 * 
 */
package com.xuggle.utils.event.handler;
