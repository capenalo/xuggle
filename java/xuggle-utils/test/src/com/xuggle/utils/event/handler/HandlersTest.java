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
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.IEventHandler;
import com.xuggle.utils.event.EventHandlerRemovedEvent;
import com.xuggle.utils.event.SynchronousEventDispatcher;

public class HandlersTest
{
  private class TestEvent extends Event
  {
    public TestEvent(Object source) { super(source); }
  }


  @Test
  public void testGetTargetedAndBoundedHandler()
  {
    final AtomicInteger numCalls = new AtomicInteger(0);
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    Object source = new Object();
    TestEvent correctEvent = new TestEvent(source);
    TestEvent wrongEvent = new TestEvent(this);

    int maxCalls = 4;
    IEventHandler<TestEvent> handler = Handler.makeTargetedAndBoundedHandler(
        source,
        maxCalls,
        new IEventHandler<TestEvent>(){
          public boolean handleEvent(IEventDispatcher dispatcher,
              com.xuggle.utils.event.handler.HandlersTest.TestEvent event)
          {
            numCalls.incrementAndGet();
            return false;
          }}
        );
    dispatcher.addEventHandler(0, TestEvent.class, handler);
    for(int i = 0; i < maxCalls; i++)
    {
      assertEquals(i, numCalls.get());
      dispatcher.dispatchEvent(correctEvent);
      dispatcher.dispatchEvent(wrongEvent);
      assertEquals(i+1, numCalls.get());
    }
    // now should no longer execute
    for(int i = 0; i < 5; i++)
    {
      assertEquals(maxCalls, numCalls.get());
      dispatcher.dispatchEvent(correctEvent);
      dispatcher.dispatchEvent(wrongEvent);
      assertEquals(maxCalls, numCalls.get());
    }
  }

  @Test
  public void testRegister()
  {
    final AtomicInteger numCalls = new AtomicInteger(0);
    final AtomicInteger numRemoveHandler = new AtomicInteger(0);
    
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    Object source = new Object();
    TestEvent correctEvent = new TestEvent(source);
    TestEvent wrongEvent = new TestEvent(this);

    int maxCalls = 4;

    dispatcher.addEventHandler(0,
        EventHandlerRemovedEvent.class,
        new IEventHandler<EventHandlerRemovedEvent>(){
          public boolean handleEvent(IEventDispatcher dispatcher,
              EventHandlerRemovedEvent event)
          {
            numRemoveHandler.incrementAndGet();
            return false;
          }});
    Handler.register(dispatcher,
        0,
        TestEvent.class,
        source,
        maxCalls,
        new IEventHandler<TestEvent>(){
          public boolean handleEvent(IEventDispatcher dispatcher,
              com.xuggle.utils.event.handler.HandlersTest.TestEvent event)
          {
            numCalls.incrementAndGet();
            return false;
          }}
        );
    for(int i = 0; i < maxCalls; i++)
    {
      assertEquals(i, numCalls.get());
      dispatcher.dispatchEvent(correctEvent);
      dispatcher.dispatchEvent(wrongEvent);
      assertEquals(i+1, numCalls.get());
    }
    // now should no longer execute
    for(int i = 0; i < 5; i++)
    {
      assertEquals(maxCalls, numCalls.get());
      dispatcher.dispatchEvent(correctEvent);
      dispatcher.dispatchEvent(wrongEvent);
      assertEquals(maxCalls, numCalls.get());
    }
    assertEquals(1, numRemoveHandler.get());
  }

}
