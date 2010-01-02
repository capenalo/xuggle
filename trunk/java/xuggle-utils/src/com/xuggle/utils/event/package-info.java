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

