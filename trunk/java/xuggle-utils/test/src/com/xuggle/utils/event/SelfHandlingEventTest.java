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

import static org.junit.Assert.*;

import org.junit.Test;

import com.xuggle.utils.Mutable;

public class SelfHandlingEventTest
{

  @Test
  public final void testSelfHandlingEvent()
  {
    new SelfHandlingEvent<IEvent>(null)
    {
      @Override
      public boolean handleEvent(IEventDispatcher aDispatcher, IEvent aEvent)
      {
        return false;
      }
    };
    new SelfHandlingEvent<IEvent>(this)
    {
      @Override
      public boolean handleEvent(IEventDispatcher aDispatcher, IEvent aEvent)
      {
        return false;
      }
    };
    
  }

  @Test
  public final void testHandleEvent()
  {
    final SynchronousEventDispatcher dispatcher = 
      new SynchronousEventDispatcher();
    final Mutable<Boolean> gotHandled = new Mutable<Boolean>(false);
    
    dispatcher.dispatchEvent(new SelfHandlingEvent<IEvent>(this)
    {
      @Override
      public boolean handleEvent(IEventDispatcher aDispatcher, IEvent aEvent)
      {
        gotHandled.set(true);
        return false;
      }
    });
    assertTrue(gotHandled.get());

  }

  @Test
  public final void testHandleEventDoesNotContinueIfHandled()
  {
    final SynchronousEventDispatcher dispatcher = 
      new SynchronousEventDispatcher();
    final Mutable<Integer> numTimesHandled = new Mutable<Integer>(0);
    
    final IEventHandler<IEvent> handler = new IEventHandler<IEvent>()
    {
      public boolean handleEvent(IEventDispatcher aDispatcher, IEvent aEvent)
      {
        numTimesHandled.set(numTimesHandled.get()+1);
        return false;
      }
      
    };
    IEvent event = new SelfHandlingEvent<IEvent>(this)
    {
      @Override
      public boolean handleEvent(IEventDispatcher aDispatcher, IEvent aEvent)
      {
        // call the handle event function, but it normally returns false
        handler.handleEvent(aDispatcher, aEvent);
        // this is the key; returning true shoudl stop execution here!
        return true;
      }
    };
    // register a listener
    dispatcher.addEventHandler(0, event.getClass(), handler);
    
    dispatcher.dispatchEvent(event);
    assertEquals(1, numTimesHandled.get().intValue());
  }

  @Test
  public final void testHandleEventDoesContinueIfNotHandled()
  {
    final SynchronousEventDispatcher dispatcher = 
      new SynchronousEventDispatcher();
    final Mutable<Integer> numTimesHandled = new Mutable<Integer>(0);
    
    final IEventHandler<IEvent> handler = new IEventHandler<IEvent>()
    {
      public boolean handleEvent(IEventDispatcher aDispatcher, IEvent aEvent)
      {
        numTimesHandled.set(numTimesHandled.get()+1);
        return false;
      }
      
    };
    IEvent event = new SelfHandlingEvent<IEvent>(this)
    {
      @Override
      public boolean handleEvent(IEventDispatcher aDispatcher, IEvent aEvent)
      {
        // call the handle event function, but it normally returns false
        handler.handleEvent(aDispatcher, aEvent);
        // this is the key; returning false should continue execution here!
        return false;
      }
    };
    // register a listener
    dispatcher.addEventHandler(0, event.getClass(), handler);
    
    dispatcher.dispatchEvent(event);
    assertEquals(2, numTimesHandled.get().intValue());
  }


}
