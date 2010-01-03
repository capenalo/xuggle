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
 * A decoupled event-dispatcher/event-handler implementation similar to the
 * EventDispatcher model used in Flash ActionScript. This set of classes can be
 * used to create <a
 * href="http://en.wikipedia.org/wiki/Event-driven_architecture"> Event Driven
 * Architecture</a> based applications where event producers and event consumers
 * are totally unaware of each other.
 * <p>
 * In fact, Xuggle's Octopus media server relies extensively on this class.
 * </p>
 */
package com.xuggle.utils.event;

