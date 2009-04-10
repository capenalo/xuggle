/*
 * Copyright (c) 2008-2009 by Xuggle Inc. All rights reserved.
 *
 * It is REQUESTED BUT NOT REQUIRED if you use this library, that you let 
 * us know by sending e-mail to info@xuggle.com telling us briefly how you're
 * using the library and what you like or don't like about it.
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.xuggle.utils.event;

/**
 * Implementing classes allow {@link IEventHandler}s to be registered
 * and will dispatch events to them.
 * 
 * @author aclarke
 *
 */

public interface IEventGenerator
{
  /**
   * Adds an event handler for the given eventClass (and
   * all children).
   * 
   * @param priority An arbitrary priority you can assign; lower numbers are higher
   *   priority.  All event handlers at a higher priority are called
   *   before lower handles.  You must match the priority in the
   *   {@link #removeEventHandler(int, Class, IEventHandler)} call as well.   
   * @param eventClass  Specifies the class of events that should be
   *   passed to this handler.  The dispatcher will ensure that the
   *   passed event is of the passed type.
   * <p><b>WARNING:</b> eventClass is only used to get a className, and
   *   dispatchEvent will only ever call your handler if the event class
   *   canonical name exactly matches the eventClass canonical name set here.
   *   In other words, if you have:</p>
   *  <p><code><pre>
   *     class Parent extends IEvent {
   *     };
   *     class Child extends Parent {
   *     };
   *     dispatcher.addEventHandler(0, Parent, handler1);
   *     dispatcher.addEventHandler(0, Child, handler2);
   *     dispatcher.dispatchEvent(new Child());
   *   </pre></code></p>
   *   only handler2 will be called.
   * @param handler The handler to call if an appropriate event is being
   *   dispatched by the dispatcher.
   */
  void addEventHandler(int priority, Class<? extends IEvent> eventClass, IEventHandler handler);

  /**
   * Removes a previously registered event class and event handler
   * combo.
   * @param priority The priority used with calling {@link #addEventHandler(int, Class, IEventHandler)}
   * @param eventClass The class passed to addEventHandler()
   * @param handler The handler passed to addEventHandler()
   * @exception IndexOutOfBoundsException Throws this if the combo of
   *   eventClass and handler is not currently registered with this
   *   dispatcher.
   */
  void removeEventHandler(int priority, Class<? extends IEvent> eventClass, IEventHandler handler)
  throws IndexOutOfBoundsException;

}
