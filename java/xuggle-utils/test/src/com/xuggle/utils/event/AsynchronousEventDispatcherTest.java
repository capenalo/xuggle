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

package com.xuggle.utils.event;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


import com.xuggle.utils.Mutable;
import com.xuggle.utils.event.AsynchronousEventDispatcher;
import com.xuggle.utils.event.Event;
import com.xuggle.utils.event.EventDispatcherAbortEvent;
import com.xuggle.utils.event.EventDispatcherStopEvent;
import com.xuggle.utils.event.IAsynchronousEventDispatcher;
import com.xuggle.utils.event.IEvent;
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.IEventHandler;

import org.junit.*;


public class AsynchronousEventDispatcherTest
{
  private long mNumEventsHandled;
  
  @Before
  public void setUp()
  {
    mNumEventsHandled = 0;
  }

  @Test(timeout=2000)
  public void testCreateDispatcher()
  {
    IAsynchronousEventDispatcher dispatcher = null;
    
    // try creating with a dispatcher thread.
    dispatcher = new AsynchronousEventDispatcher(true);
    assertTrue(dispatcher != null);
    assertTrue(dispatcher.isDispatching());
    dispatcher.stopDispatching();
    dispatcher.waitForDispatcherToFinish(0);
    assertTrue(!dispatcher.isDispatching());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testAddEventHandler()
  {
    IAsynchronousEventDispatcher dispatcher = new AsynchronousEventDispatcher(false);
    assertTrue(dispatcher != null);

    IEventHandler handler = new IEventHandler()
    {
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        return false;
      }
    };
    Class clazz = EventDispatcherStopEvent.class;
    dispatcher.addEventHandler(0, clazz, handler);
    dispatcher.removeEventHandler(0, clazz, handler);
  }

  @SuppressWarnings("unchecked")
  @Test(expected=IndexOutOfBoundsException.class)
  public void testRemoveEventHandler() throws IndexOutOfBoundsException
  {
    IAsynchronousEventDispatcher dispatcher = new AsynchronousEventDispatcher(false);
    assertTrue(dispatcher != null);

    IEventHandler handler = new IEventHandler()
    {
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        return false;
      }
    };
    Class clazz = EventDispatcherStopEvent.class;
    dispatcher.addEventHandler(0, clazz, handler);
    dispatcher.removeEventHandler(1, clazz, handler);
  }
  
  @Test
  public void testDispatchEvent()
  {
    class TestEvent extends Event
    {
      public TestEvent() { super(null); }
      public boolean mWasHandled = false;
    };
    IEventHandler<TestEvent> handler = new IEventHandler<TestEvent>()
    {
      public boolean handleEvent(IEventDispatcher dispatcher, TestEvent event)
      {
        event.mWasHandled = true;
        return true;
      }
    };
    // add the handler
    IAsynchronousEventDispatcher dispatcher = new AsynchronousEventDispatcher(false);
    assertTrue(dispatcher != null);
    dispatcher.addEventHandler(0, TestEvent.class, handler);
    TestEvent event = new TestEvent();
    assertTrue(!event.mWasHandled);
    dispatcher.dispatchEvent(event);
    assertTrue(!event.mWasHandled);
    // now start dispatching
    dispatcher.startDispatching();
    // now stop
    dispatcher.stopDispatching();
    // wait
    dispatcher.waitForDispatcherToFinish(0);
    // and ensure we were handled
    assertTrue(event.mWasHandled);
    dispatcher.removeEventHandler(0, TestEvent.class, handler);
  }
  
  @Test(timeout=5000)
  public void testStopDispatchingOnceOneHandlerHandles()
  {
    class TestEvent extends Event
    {
      public TestEvent() { super(null); }
      public int mHandlerNo = -1;
    };
    IEventHandler<IEvent> handler1 = new IEventHandler<IEvent>()
    {
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        TestEvent ev = (TestEvent) event;
        ev.mHandlerNo = 1;
        return true;
      }
    };
    IEventHandler<IEvent> handler2 = new IEventHandler<IEvent>()
    {
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        TestEvent ev = (TestEvent) event;
        ev.mHandlerNo = 2;
        return true;
      }
    };
    // add the handlers
    IAsynchronousEventDispatcher dispatcher = new AsynchronousEventDispatcher(true);
    assertTrue(dispatcher != null);
    dispatcher.addEventHandler(1, TestEvent.class, handler1);
    // 2 is higher priority, and hence should always trump 1
    dispatcher.addEventHandler(0, TestEvent.class, handler2);

    TestEvent event = new TestEvent();
    assertTrue(event.mHandlerNo == -1);
    dispatcher.dispatchEvent(event);
    // stop dispatching
    dispatcher.stopDispatching();
    // wait
    dispatcher.waitForDispatcherToFinish(0);
    assertTrue(event.mHandlerNo == 2);
  }
  
  @Test(timeout=5000)
  public void testDispatcherProcessesEarlierEventsBeforeStop()
  {
    class TestEvent extends Event
    {
      public TestEvent() { super(null); }
    };
    IEventHandler<IEvent> handler = new IEventHandler<IEvent>()
    {
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        mNumEventsHandled++;
        return true;
      }
    };
    mNumEventsHandled = 0;
    IAsynchronousEventDispatcher dispatcher = new AsynchronousEventDispatcher();
    assertTrue(dispatcher != null);
    dispatcher.addEventHandler(1, TestEvent.class, handler);
    assertTrue(mNumEventsHandled == 0);
    int numEventsToProcess = 100;
    for (int i = 0; i < numEventsToProcess; i++)
      dispatcher.dispatchEvent(new TestEvent());
    // and after all that, tell it to stop
    dispatcher.dispatchEvent(new EventDispatcherStopEvent(this));
    // and let's add another set of events afterwards which should
    // be dropped
    for (int i = 0; i < numEventsToProcess; i++)
      dispatcher.dispatchEvent(new TestEvent());
    
    // now, start doing the actual processing
    dispatcher.startDispatching();
    // wait for it to finish
    dispatcher.waitForDispatcherToFinish(0);
    assertTrue(mNumEventsHandled == numEventsToProcess);

    // reset the number of events handled
    mNumEventsHandled = 0;
    // now test that an abort means we'll immediately exit
    // make sure we got the number of events expected
    for (int i = 0; i < numEventsToProcess; i++)
      dispatcher.dispatchEvent(new TestEvent());
    // and after all that, tell it to ABORT.  this should mean
    // it'll ignore all events...
    dispatcher.dispatchEvent(new EventDispatcherAbortEvent(this));
    
    // and let's add another set of events afterwards which should
    // be dropped
    for (int i = 0; i < numEventsToProcess; i++)
      dispatcher.dispatchEvent(new TestEvent());
    
    // now, start doing the actual processing
    dispatcher.startDispatching();
    // wait for it to finish
    dispatcher.waitForDispatcherToFinish(0);
    // make sure we got the number of events expected
    assertTrue("abort should process before any events", 0 == mNumEventsHandled);
  }
  
  /**
   * Tests that events dispatched inside an event handler DO NOT
   * always get executed before events added from another thread.
   * @throws InterruptedException 
   */
  
  @Test(timeout=10000)
  public void testEventsDispatchedInsideEventHandlerNoLongerAlwaysGetHandledBeforeOtherEventsAndIsThisFunctionNameLongEnoughIDoNotThinkSo() throws InterruptedException
  {
    IAsynchronousEventDispatcher dispatcher = new AsynchronousEventDispatcher();
    dispatcher.startDispatching();
    
    final Mutable<Boolean> secondOutsideEventHandled=new Mutable<Boolean>(false);
    final Mutable<Boolean> testSucceeded = new Mutable<Boolean>(false);
    final Object lock = new Object();
    // make sure our events added OUTSIDE the lock are
    // added before their handlers execute
    synchronized(lock) {
      ISelfHandlingEvent<IEvent> outsideEvent = new SelfHandlingEvent<IEvent>(this)
      {
        @Override
        public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
        {
          synchronized(lock)
          {
            ISelfHandlingEvent<IEvent> insideEvent = new SelfHandlingEvent<IEvent>(this)
            {
              @Override
              public boolean handleEvent(IEventDispatcher dispatcher,
                  IEvent event)
              {
                testSucceeded.set(secondOutsideEventHandled.get());
                return false;
              }
            };
            dispatcher.dispatchEvent(insideEvent);
            lock.notify();
          }
          return false;
        }
        
      };
      dispatcher.dispatchEvent(outsideEvent);
      ISelfHandlingEvent<IEvent> secondOutsideEvent =
        new SelfHandlingEvent<IEvent>(this){
          @Override
          public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
          {
            secondOutsideEventHandled.set(true);
            return false;
          }};
      dispatcher.dispatchEvent(secondOutsideEvent);    
      lock.wait();
    }
    // by waiting above we guarantee that all events have been dispatched
    // but not necessarily handled
    dispatcher.stopDispatching();
    dispatcher.waitForDispatcherToFinish(0);
    assertTrue(secondOutsideEventHandled.get());
    assertTrue("should execute AFTER the secondEvent", 
        testSucceeded.get());
    
  }
  
  @Test(timeout=2000)
  public void testDeleteIsCalledWhenDispatchFinished()
  {
    final AtomicBoolean deleteCalled = new AtomicBoolean(false);
    final IEvent event = new Event(this){
      @Override
      public void delete()
      {
        deleteCalled.set(true);
      }
    };
    final IAsynchronousEventDispatcher dispatcher =
      new AsynchronousEventDispatcher();
    assertFalse(deleteCalled.get());
    dispatcher.startDispatching();
    dispatcher.dispatchEvent(event);
    dispatcher.stopDispatching();
    dispatcher.waitForDispatcherToFinish(0);
    assertTrue(deleteCalled.get());
  }

  @Test(timeout=2000*1000)
  public void testDeleteIsCalledAfterAllDispatches()
  {
    final AtomicLong numDeletes = new AtomicLong(0);
    final AtomicLong numDispatches = new AtomicLong(0);
    final long totalDispatches = 5;
    final IEvent event = new SelfHandlingEvent<IEvent>(this){

      @Override
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        if(numDispatches.incrementAndGet() < totalDispatches)
          dispatcher.dispatchEvent(this);
        else
          ((IAsynchronousEventDispatcher)dispatcher).stopDispatching();
        return false;
      }
      
      @Override
      public void delete()
      {
        numDeletes.incrementAndGet();
        super.delete();
      }
    };
    final IAsynchronousEventDispatcher dispatcher =
      new AsynchronousEventDispatcher();
    assertEquals(0, numDeletes.get());
    assertEquals(0, numDispatches.get());
    dispatcher.startDispatching();
    dispatcher.dispatchEvent(event);
    dispatcher.waitForDispatcherToFinish(0);
    assertEquals(1, numDeletes.get());
    assertEquals(totalDispatches, numDispatches.get());
    
  }
  
  @Test(timeout=2000*1000)
  public void testDeleteCalledWhenAborted()
  {
    final AtomicLong numDeletes = new AtomicLong(0);
    final AtomicLong numHandles = new AtomicLong(0);
    final long totalDispatches = 5;
    final IEvent deletingEvent = new SelfHandlingEvent<IEvent>(this){
      @Override
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        numHandles.incrementAndGet();
        return false;
      }
      @Override
      public void delete()
      {
        numDeletes.incrementAndGet();
      }

    };
    final IEvent testEvent= new SelfHandlingEvent<IEvent>(this)
    {
      @Override
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        // Queue up a bunch of our test events, and then
        // abort
        for(int i = 0; i < totalDispatches; i++)
          dispatcher.dispatchEvent(deletingEvent);
        dispatcher.dispatchEvent(new EventDispatcherAbortEvent(this));
        return false;
      }
      
    };
    final IAsynchronousEventDispatcher dispatcher =
      new AsynchronousEventDispatcher();
    assertEquals(0, numDeletes.get());
    assertEquals(0, numHandles.get());
    
    // we queue up a bunch of events
    dispatcher.startDispatching();
    dispatcher.dispatchEvent(testEvent);
    dispatcher.waitForDispatcherToFinish(0);
    assertEquals(0, numHandles.get());
    assertEquals(1, numDeletes.get());
  }
}
