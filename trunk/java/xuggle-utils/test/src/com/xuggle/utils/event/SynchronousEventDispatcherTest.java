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
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;


import com.xuggle.utils.Mutable;
import com.xuggle.utils.event.Event;
import com.xuggle.utils.event.EventDispatcherStopEvent;
import com.xuggle.utils.event.IEvent;
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.IEventHandler;
import com.xuggle.utils.event.SynchronousEventDispatcher;

import org.junit.*;

public class SynchronousEventDispatcherTest
{

  private int mNumEventsHandled = 0;
  
  @Before
  public void setUp()
  {
    mNumEventsHandled = 0;
  }

  @Test(timeout=2000)
  public void testCreateDispatcher()
  {
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    assertTrue(dispatcher != null);
  }

  @Test
  public void testAddEventHandler()
  {
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    assertTrue(dispatcher != null);

    IEventHandler<IEvent> handler = new IEventHandler<IEvent>()
    {
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        return false;
      }
    };
    Class<? extends IEvent> clazz = EventDispatcherStopEvent.class;
    dispatcher.addEventHandler(0, clazz, handler);
    dispatcher.removeEventHandler(0, clazz, handler);
  }

  @Test(expected=IndexOutOfBoundsException.class)
  public void testRemoveEventHandler() throws IndexOutOfBoundsException
  {
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    assertTrue(dispatcher != null);

    IEventHandler<IEvent> handler = new IEventHandler<IEvent>()
    {
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        return false;
      }
    };
    Class<? extends IEvent> clazz = EventDispatcherStopEvent.class;
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
    IEventHandler<IEvent> handler = new IEventHandler<IEvent>()
    {
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        TestEvent ev = (TestEvent) event;
        ev.mWasHandled = true;
        return true;
      }
    };
    // add the handler
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    assertTrue(dispatcher != null);
    dispatcher.addEventHandler(0, TestEvent.class, handler);
    TestEvent event = new TestEvent();
    assertTrue(!event.mWasHandled);
    dispatcher.dispatchEvent(event);
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
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    assertTrue(dispatcher != null);
    dispatcher.addEventHandler(1, TestEvent.class, handler1);
    // 2 is higher priority, and hence should always trump 1
    dispatcher.addEventHandler(0, TestEvent.class, handler2);

    TestEvent event = new TestEvent();
    assertTrue(event.mHandlerNo == -1);
    dispatcher.dispatchEvent(event);
    assertTrue(event.mHandlerNo == 2);
  }
  
  @Test
  public void testHandlerForParentClassesNotCalledForSubEvents()
  {
    class Parent extends Event
    {
      public Parent() { super(null); }
    }
    class Uncle extends Event
    {
      public Uncle() { super(null); }
    }
    class Aunt extends Event
    {
      @SuppressWarnings("unused")
      public Aunt() { super(null); }
    }
    class Child extends Parent
    {      
    }
    IEventHandler<IEvent> handler = new IEventHandler<IEvent>()
    {
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        mNumEventsHandled++;
        return false;
      }
    };
    mNumEventsHandled = 0;
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    assertTrue(dispatcher != null);
    dispatcher.addEventHandler(1, Parent.class, handler);
    dispatcher.addEventHandler(1, Child.class, handler);
    dispatcher.addEventHandler(1, Aunt.class, handler);

    dispatcher.dispatchEvent(new Parent()); // handler should be called once
    dispatcher.dispatchEvent(new Child()); // handler should be called once
    dispatcher.dispatchEvent(new Child()); // handler should be called once
    dispatcher.dispatchEvent(new Uncle()); // handler should not be called

    assertTrue(mNumEventsHandled == 3);
  }
  
  @Test
  public void testDispatchFromHandlerIsDeferred()
  {
    class TestEvent extends Event
    {
      public TestEvent() { super(null); }
      public int mNumDispatches = 0;
    };
    IEventHandler<IEvent> handler = new IEventHandler<IEvent>()
    {
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        TestEvent ev = (TestEvent) event;
        
        // Cache the value BEFORE we call another dispatchEvent
        int numDispatches = ev.mNumDispatches;

        // we know dispatch was called once
        ++ev.mNumDispatches;

        if (numDispatches == 0) {
          // This is the first time through

          // dispatch it again...
          dispatcher.dispatchEvent(event);

          // If working correctly, the dispatchEvent above should
          // be deferred until after this handler executes, and so
          // only one increment should happen
          assertTrue(ev.mNumDispatches == 1);
        }
        return true;
      }
    };
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    assertTrue(dispatcher != null);

    dispatcher.addEventHandler(0, TestEvent.class, handler);
    
    TestEvent event = new TestEvent();
    assertTrue(event.mNumDispatches == 0);
    dispatcher.dispatchEvent(event);
    // the stack should unwind and call the 2nd dispatch event
    // before returning from above
    assertTrue(event.mNumDispatches == 2);    
  }
  
  @Test
  public void testEventHandlerCanRemoveEventHandler()
  {
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    assertTrue(dispatcher != null);

    class TestEvent extends Event
    {
      public TestEvent(Object source)
      {
        super(source);
      }
    };
    class TestEventHandler implements IEventHandler<TestEvent>
    {
      
      public boolean handleEvent(IEventDispatcher aDispatcher, TestEvent aEvent)
      {
        aDispatcher.removeEventHandler(0, TestEvent.class, this);
        return false;
      }
    }
    dispatcher.addEventHandler(0, TestEvent.class, new TestEventHandler());
    dispatcher.dispatchEvent(new TestEvent(this));
    // we should get here without a failure.
  }

  /**
   * Tests remove an event handler that has not been
   * added, but at a priority and event class that
   * already has been added
   */
  @Test(expected=IndexOutOfBoundsException.class)
  public void testRemoveEventHandlerMissingHandler()
  {
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    assertTrue(dispatcher != null);

    IEventHandler<IEvent> handler = new IEventHandler<IEvent>()
    {
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        return false;
      }
    };
    Class<? extends IEvent> clazz = EventDispatcherStopEvent.class;
    dispatcher.addEventHandler(0, clazz, handler);
    // This should fail
    dispatcher.removeEventHandler(0, clazz, new IEventHandler<IEvent>(){
      
      public boolean handleEvent(IEventDispatcher aDispatcher, IEvent aEvent)
      {
       return false;
      }
      
    });
  }
  @Test
  public void testEventCalledBeforeRemovalAndNotAfterwards()
  {
    AtomicInteger numCalls = new AtomicInteger(0);
    
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    assertTrue(dispatcher != null);

    class TestEvent extends Event
    {
      public TestEvent(Object source)
      {
        super(source);
      }
    };
    class TestEventHandler implements IEventHandler<TestEvent>
    {
      AtomicInteger mCount;
      public TestEventHandler(AtomicInteger count)
      {
        mCount = count;
      }
      
      public boolean handleEvent(IEventDispatcher aDispatcher, TestEvent aEvent)
      {
        mCount.incrementAndGet();
        aDispatcher.removeEventHandler(0, TestEvent.class, this);
        return false;
      }
    }
    dispatcher.addEventHandler(0, TestEvent.class, new TestEventHandler(numCalls));
    assertEquals("should not yet be handled", 0, numCalls.get());
    dispatcher.dispatchEvent(new TestEvent(this));
    assertEquals("should be handled once", 1, numCalls.get());
    dispatcher.dispatchEvent(new TestEvent(this));
    assertEquals("should not be handled again after first handler removes", 1, numCalls.get());
  }

  /**
   * Tests that if we pass a handler for a class with the wrong
   * templated type, that a class cast exception will be thrown when
   * dispatching
   * 
   */
  
  @Test(expected=ClassCastException.class)
  public void testAddEventHandlerOfWrongType()
  {
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    class TestEvent extends Event
    {
      @SuppressWarnings("unused")
      public TestEvent(Object source) { super(source); }
    }
    IEventHandler<TestEvent> wrongHandler = new IEventHandler<TestEvent>(){

      public boolean handleEvent(IEventDispatcher dispatcher, TestEvent event)
      {
        // should never get called
        fail("shouldn't be here");
        return false;
      }};
      IEvent wrongEvent = new Event(this){};
      dispatcher.addEventHandler(0, wrongEvent.getClass(), wrongHandler);
      dispatcher.dispatchEvent(wrongEvent);
  }
  
  @Test
  public void testAddEventHandlersHappenImmediately()
  {
    final IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    final Mutable<Boolean> gotFirstEvent = new Mutable<Boolean>(false);
    final Mutable<Boolean> gotSecondEvent = new Mutable<Boolean>(false);
    
    final ISelfHandlingEvent<IEvent> firstEvent =
      new SelfHandlingEvent<IEvent>(this){
      @Override
      public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
      {
        final IEvent secondEvent = new Event(this){};
        // register an event handler; in old code this would not fire
        // until this handleEvent has fully unwound
        // dispatch the second event
        dispatcher.dispatchEvent(secondEvent);
        dispatcher.addEventHandler(0, secondEvent.getClass(), 
            new IEventHandler<IEvent>(){
              public boolean handleEvent(IEventDispatcher dispatcher,
                  IEvent event)
              {
                assertTrue(gotFirstEvent.get());
                gotSecondEvent.set(true);
                dispatcher.removeEventHandler(0, event.getClass(), this);
                return false;
              }});
        gotFirstEvent.set(true);
        return false;
      }};
      dispatcher.dispatchEvent(firstEvent);
      assertTrue(gotFirstEvent.get());
      assertTrue(gotSecondEvent.get());
  }
}
