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
  
  public static <T extends Event> void handleOnce(
      final Class<T> eventClass,
      final IEventHandlerRegistrable registry,
      final Object source,
      final int priority,
      final IEventHandler<T> handler)
  {
    if (registry == null || priority < 0 || handler == null)
      throw new IllegalArgumentException();
    
    registry.addEventHandler(priority,
        eventClass,
        new IEventHandler<T>()
        {
          public boolean handleEvent(IEventDispatcher dispatcher, T event)
          {
            if (source != null && !source.equals(event.getSource()))
              return false;
            // de-register ourselves
            registry.removeEventHandler(0, 
                eventClass, this);
            // and call the handler
            return handler.handleEvent(dispatcher, event);
          }
      
        }
    );
  }

}
