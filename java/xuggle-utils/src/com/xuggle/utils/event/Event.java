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
 * A base implementation of an Event.  Can be handy to extend from.
 */
public abstract class Event implements IEvent
{

  private final Object mSource;
  private final long mNow;

  public Event(Object aSource)
  {
    mSource = aSource;
    mNow = System.nanoTime();
  }
  public Object getSource()
  {
    return mSource;
  }
  public long getWhen()
  {
    return mNow;
  }
  
  /**
   * Override this method and describe your event.  Make sure
   * you call your super.getDescription() and you'll get nicely
   * formatted event strings.
   * <p>
   * If you want to nest another object, the general format an object
   * log value should take is:
   * </p>
   * <code>
   * classname@uniquecode[field=value;field=value;field=value]
   * </code>
   * <p>
   * For example:
   * </p>
   * <code>
   * com.xuggle.utils.event.Event@16ac176[source=com.xuggle.utils.event.SychronousEventDispatcher@168282d;]
   * </code>
   * 
   * @return a description of your event of the form "field=value;field=value;"
   */
  public String getDescription()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("source=").append(getSource()).append(";");
    builder.append("when=").append(getWhen()).append(";");
    return builder.toString();
  }
  
  /**
   * Return a string representation of this event.
   * <p>
   * You cannot override toString on an Event.  Instead, override
   * {@link #getDescription()}, and call super.{@link #getDescription()}
   * before adding your own name/value output pairs.  This way events
   * will be printed in a standard format.
   * </p>
   * @return a string
   */
  @Override
  public final String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(super.toString());
    builder.append("[");
    builder.append(getDescription());
    builder.append("]");
    return builder.toString();
  }
}
