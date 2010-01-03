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

package com.xuggle.utils.event.handler;

import static org.junit.Assert.*;

import org.junit.Test;

import com.xuggle.utils.Mutable;
import com.xuggle.utils.event.Event;
import com.xuggle.utils.event.IEvent;
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.IEventHandler;
import com.xuggle.utils.event.SynchronousEventDispatcher;
import com.xuggle.utils.event.handler.ForwardingHandler;

public class ForwardingHandlerTest
{

  @Test
  public final void testForwardingHandler()
  {
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    try
    {
      new ForwardingHandler(null);
      fail("should never get here");
    } catch (IllegalArgumentException e) {}
    new ForwardingHandler(dispatcher);
  }

  @Test
  public final void testGetDestinationDispatcher()
  {
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    ForwardingHandler handler = new ForwardingHandler(dispatcher);
    assertEquals(dispatcher, handler.getDestinationDispatcher());
  }

  @Test
  public final void testHandleEvent()
  {
    IEventDispatcher fromDispatcher = new SynchronousEventDispatcher();
    IEventDispatcher toDispatcher = new SynchronousEventDispatcher();
    ForwardingHandler handler = new ForwardingHandler(toDispatcher);
    
    IEvent event = new Event(this){};

    // register the forwarding handler
    fromDispatcher.addEventHandler(0, event.getClass(), handler);

    final Mutable<Boolean> wasForwarded = new Mutable<Boolean>(false);
    
    // and register the actual handler
    toDispatcher.addEventHandler(0, event.getClass(),
        new IEventHandler<IEvent>() {
          public boolean handleEvent(IEventDispatcher aDispatcher, IEvent aEvent)
          {
            wasForwarded.set(true);
            return false;
          }
    });
    
    fromDispatcher.dispatchEvent(event);
    
    assertTrue(wasForwarded.get());
  }

  @Test
  public final void testHandleEventButForwardingToSameDispatcher()
  {
    IEventDispatcher fromDispatcher = new SynchronousEventDispatcher();
    IEventDispatcher toDispatcher = fromDispatcher;
    ForwardingHandler handler = new ForwardingHandler(toDispatcher);
    
    IEvent event = new Event(this){};

    // register the forwarding handler
    fromDispatcher.addEventHandler(0, event.getClass(), handler);

    final Mutable<Boolean> wasForwarded = new Mutable<Boolean>(false);
    
    // and register the actual handler
    toDispatcher.addEventHandler(0, event.getClass(),
        new IEventHandler<IEvent>() {
          public boolean handleEvent(IEventDispatcher aDispatcher, IEvent aEvent)
          {
            wasForwarded.set(true);
            return false;
          }
    });
    
    fromDispatcher.dispatchEvent(event);
    
    assertTrue(wasForwarded.get());
  }

}
