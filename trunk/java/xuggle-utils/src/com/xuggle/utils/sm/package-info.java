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
 * The Xuggle State Machine (sm) implementation.  State machines
 * are a well used way to manage Objects that have changing state and
 * need to respond to a common set of options while in different states.
 * See <a href="http://www.google.com/url?sa=t&source=web&ct=res&cd=2&ved=0CA8QFjAB&url=http%3A%2F%2Fdotnet.zcu.cz%2FNET_2006%2FPapers_2006%2Fshort%2FB31-full.pdf&ei=7ac_S4LlKIu6swO908DMBA&usg=AFQjCNHw82LeBmQ401rtQfs3BERamQWT_A&sig2=KyC3aaLyfL7C1xzBU5EY_g">this paper</a>
 * for an example of a way to implement them.
 * <p>
 * The Xuggle State Machine implementation is integrated with Xuggle's
 * asynchronous {@link com.xuggle.utils.event.IEvent} system to throw
 * {@link com.xuggle.utils.event.IEvent} objects when state transitions occur.
 * </p>
 * <p>
 * For an example of a State Machine implementation, see the set of State Machine
 * tests in the test directory of the source for this library.
 * </p>
 */
package com.xuggle.utils.sm;

