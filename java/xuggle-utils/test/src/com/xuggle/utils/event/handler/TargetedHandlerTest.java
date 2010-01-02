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

package com.xuggle.utils.event.handler;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.xuggle.utils.event.Event;
import com.xuggle.utils.event.IEvent;
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.IEventHandler;
import com.xuggle.utils.event.MockNullEventHandler;
import com.xuggle.utils.event.SynchronousEventDispatcher;

public class TargetedHandlerTest
{

  @Test
  public final void testTargetedHandler()
  {
    try
    {
      new TargetedHandler<IEvent>(null, null);
      fail("shouldn't get here");
    }catch(IllegalArgumentException e){}
    new TargetedHandler<IEvent>(null,
        new MockNullEventHandler<IEvent>());
  }

  @Test
  public final void testGetTargetedSource()
  {
    IEventHandler<IEvent> targetedHandler =
      new MockNullEventHandler<IEvent>();
    Object source = new Object();
    TargetedHandler<IEvent> handler = 
      new TargetedHandler<IEvent>(source, targetedHandler);
    assertEquals(source, handler.getTargetedSource());
  }

  @Test
  public final void testGetProxiedHandler()
  {
    IEventHandler<IEvent> targetedHandler =
      new MockNullEventHandler<IEvent>();
    Object source = new Object();
    TargetedHandler<IEvent> handler = 
      new TargetedHandler<IEvent>(source, targetedHandler);
    assertEquals(targetedHandler, handler.getProxiedHandler());
  }

  @Test
  public final void testHandleEvent()
  {
    final AtomicInteger numCalls = new AtomicInteger(0);
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    Object source = new Object();
    class TestEvent extends Event
    {
      public TestEvent(Object source) { super(source); }
    }
    TestEvent rightTargetEvent = new TestEvent(source);
    TestEvent wrongTargetEvent = new TestEvent(this);
    IEventHandler<TestEvent> targetedHandler = 
      new IEventHandler<TestEvent>(){
        public boolean handleEvent(IEventDispatcher dispatcher,
            TestEvent event)
        {
          numCalls.incrementAndGet();
          return false;
        }};
    TargetedHandler<TestEvent> handler = 
      Handler.makeTargetedHandler(source, targetedHandler);
    dispatcher.addEventHandler(0,
        TestEvent.class, handler);
    assertEquals(0, numCalls.get());
    dispatcher.dispatchEvent(rightTargetEvent);
    assertEquals(1, numCalls.get());
    dispatcher.dispatchEvent(wrongTargetEvent);
    assertEquals(1, numCalls.get());
    dispatcher.dispatchEvent(rightTargetEvent);
    assertEquals(2, numCalls.get());
  }

}
