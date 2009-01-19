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

import static junit.framework.Assert.assertTrue;


import com.xuggle.test_utils.NameAwareTestClassRunner;
import com.xuggle.utils.event.AsynchronousEventDispatcher;
import com.xuggle.utils.event.Event;
import com.xuggle.utils.event.EventDispatcherAbortEvent;
import com.xuggle.utils.event.EventDispatcherStopEvent;
import com.xuggle.utils.event.IAsynchronousEventDispatcher;
import com.xuggle.utils.event.IEvent;
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.IEventHandler;

import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(NameAwareTestClassRunner.class)
public class AsynchronousEventDispatcherTest
{
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private String mTestName = null;
  private long mNumEventsHandled;
  
  @Before
  public void setUp()
  {
    mTestName = NameAwareTestClassRunner.getTestMethodName();
    log.debug("Running test: {}", mTestName);
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
    IEventHandler handler = new IEventHandler()
    {
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        TestEvent ev = (TestEvent) event;
        ev.mWasHandled = true;
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
    IEventHandler handler1 = new IEventHandler()
    {
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        TestEvent ev = (TestEvent) event;
        ev.mHandlerNo = 1;
        return true;
      }
    };
    IEventHandler handler2 = new IEventHandler()
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
    IEventHandler handler = new IEventHandler()
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
}
