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
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


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
    IEventHandlerRegistrable.Key key = dispatcher.addEventHandler(0, clazz, handler);
    dispatcher.removeEventHandler(key);
  }

  @Test
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
    IEventHandlerRegistrable.Key key = dispatcher.addEventHandler(0, clazz, handler);
    dispatcher.removeEventHandler(key);
    try {
      dispatcher.removeEventHandler(new IEventHandlerRegistrable.Key(){});
      Assert.fail("should not get here");
    } catch (IndexOutOfBoundsException e){}

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
    IEventHandlerRegistrable.Key key = dispatcher.addEventHandler(0, TestEvent.class, handler);
    TestEvent event = new TestEvent();
    assertTrue(!event.mWasHandled);
    dispatcher.dispatchEvent(event);
    assertTrue(event.mWasHandled);
    dispatcher.removeEventHandler(key);
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
    dispatcher.addEventHandler(2, TestEvent.class, handler2);

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
    final AtomicReference<IEventHandlerRegistrable.Key> key =
      new AtomicReference<IEventHandlerRegistrable.Key>(null);
    class TestEventHandler implements IEventHandler<TestEvent>
    {

      public boolean handleEvent(IEventDispatcher aDispatcher, TestEvent aEvent)
      {
        aDispatcher.removeEventHandler(key.get());
        return false;
      }
    }
    key.set(dispatcher.addEventHandler(0, TestEvent.class, new TestEventHandler()));
    dispatcher.dispatchEvent(new TestEvent(this));
    // we should get here without a failure.
  }

  @Test
  public void testEventCalledBeforeRemovalAndNotAfterwards()
  {
    AtomicInteger numCalls = new AtomicInteger(0);

    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    assertTrue(dispatcher != null);
    final AtomicReference<IEventHandlerRegistrable.Key> key =
      new AtomicReference<IEventHandlerRegistrable.Key>(null);

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
        aDispatcher.removeEventHandler(key.get());
        return false;
      }
    }
    key.set(dispatcher.addEventHandler(0, TestEvent.class, new TestEventHandler(numCalls)));
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

  public void testAddEventHandlerOfWrongType()
  {
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();

    class TestEvent extends Event
    {
      @SuppressWarnings("unused")
      public TestEvent(Object source) { super(source); }
    }
    final IEventHandler<TestEvent> wrongHandler = new IEventHandler<TestEvent>()
    {
      public boolean handleEvent(IEventDispatcher dispatcher, TestEvent event)
      {
        // should never get called
        fail("shouldn't be here");
        return false;
      }
    };
    final AtomicBoolean testPassed = new AtomicBoolean(false);
    // NOTICE THIS IS NOT OF THE TYPE TestEvent; That's the test
    final IEvent wrongEvent = new Event(this){};
    dispatcher.addEventHandler(0,
        ErrorEvent.class,
        new IEventHandler<ErrorEvent>(){
      public boolean handleEvent(IEventDispatcher dispatcher,
          ErrorEvent event)
      {
        Throwable t = event.getException();
        assertNotNull(t);
        assertTrue(t instanceof ClassCastException);
        assertEquals(wrongEvent, event.getEvent());
        assertEquals(wrongHandler, event.getHandler());
        testPassed.set(true);
        return false;
      }}
    );

    dispatcher.addEventHandler(0, wrongEvent.getClass(), wrongHandler);
    dispatcher.dispatchEvent(wrongEvent);
    assertTrue(testPassed.get());
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
        final AtomicReference<IEventHandlerRegistrable.Key> key =
          new AtomicReference<IEventHandlerRegistrable.Key>(null);

        key.set(dispatcher.addEventHandler(0, secondEvent.getClass(), 
            new IEventHandler<IEvent>(){
          public boolean handleEvent(IEventDispatcher dispatcher,
              IEvent event)
          {
            assertTrue(gotFirstEvent.get());
            gotSecondEvent.set(true);
            dispatcher.removeEventHandler(key.get());
            return false;
          }}));
        gotFirstEvent.set(true);
        return false;
      }};
      dispatcher.dispatchEvent(firstEvent);
      assertTrue(gotFirstEvent.get());
      assertTrue(gotSecondEvent.get());
  }

  @Test
  public void testAddAndRemoveEventHandlersFireCorrectEvents()
  {
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();

    final AtomicInteger numAdds = new AtomicInteger(0);
    final AtomicInteger numRemoves = new AtomicInteger(0);

    IEventHandler<EventHandlerAddedEvent> addHandler = 
      new IEventHandler<EventHandlerAddedEvent>(){
      public boolean handleEvent(IEventDispatcher dispatcher,
          EventHandlerAddedEvent event)
      {
        numAdds.incrementAndGet();
        return false;
      }
    };
    IEventHandler<EventHandlerRemovedEvent> removeHandler =
      new IEventHandler<EventHandlerRemovedEvent>(){

      public boolean handleEvent(IEventDispatcher dispatcher,
          EventHandlerRemovedEvent event)
      {
        numRemoves.incrementAndGet();
        return false;
      }};

      IEventHandlerRegistrable.Key key1=
        dispatcher.addEventHandler(0, EventHandlerAddedEvent.class,
          addHandler
      );
      IEventHandlerRegistrable.Key key2= 
        dispatcher.addEventHandler(0, EventHandlerRemovedEvent.class,
          removeHandler
      );
      dispatcher.removeEventHandler(key1);
      dispatcher.removeEventHandler(key2);
      assertEquals(2, numAdds.get());
      assertEquals(1, numRemoves.get()); // shouldn't get the last remove
  }
  /**
   * This test can take a LONG time to execute; it requires the
   * Java garbage collector to collect a reference, hence the
   * long timeout for slow machines. 
   */

  @Test(timeout=1000*60*5) // 5 minute timeout.
  public void testAddEventHandlerWeakReference()
  {
    class TestEvent extends Event {
      public TestEvent(Object source) { super(source); }
    }
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();

    final AtomicInteger numAddEventHandlers = new AtomicInteger(0);
    final AtomicInteger numRemoveEventHandlers = new AtomicInteger(0);
    final AtomicBoolean gotWeakReferenceCleanup = new AtomicBoolean(false);
    dispatcher.addEventHandler(0, EventHandlerAddedEvent.class,
        new IEventHandler<EventHandlerAddedEvent>(){
      public boolean handleEvent(IEventDispatcher dispatcher,
          EventHandlerAddedEvent event)
      {
        numAddEventHandlers.incrementAndGet();
        return false;
      }});
    dispatcher.addEventHandler(0, EventHandlerRemovedEvent.class,
        new IEventHandler<EventHandlerRemovedEvent>(){
      public boolean handleEvent(IEventDispatcher dispatcher,
          EventHandlerRemovedEvent event)
      {
        numRemoveEventHandlers.incrementAndGet();
        assertNull("should only be called when removing weak reference",
            event.getHandler());
        gotWeakReferenceCleanup.getAndSet(true);
        return false;
      }});


    {
      // must be different scope
      IEventHandler<IEvent> weakHandler =
        new IEventHandler<IEvent>()
        {
        public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
        {
          return false;
        }

        };
        // add with a weak reference
        dispatcher.addEventHandler(0, TestEvent.class, weakHandler, true);
        // for shits and giggles do one dispatch
        dispatcher.dispatchEvent(new TestEvent(this));
        // kill the reference here
        weakHandler = null;
    }
    while(!gotWeakReferenceCleanup.get())
    {
      dispatcher.dispatchEvent(new TestEvent(this));
      System.gc();
    }
    assertEquals(3, numAddEventHandlers.get());
    assertEquals(1, numRemoveEventHandlers.get());
  }
  
  @Test
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
    final IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    assertFalse(deleteCalled.get());
    dispatcher.dispatchEvent(event);
    assertTrue(deleteCalled.get());
  }

  @Test
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
        return false;
      }
      
      @Override
      public void delete()
      {
        numDeletes.incrementAndGet();
        super.delete();
      }
    };
    final IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    assertEquals(0, numDeletes.get());
    assertEquals(0, numDispatches.get());
    dispatcher.dispatchEvent(event);
    assertEquals(1, numDeletes.get());
    assertEquals(totalDispatches, numDispatches.get());
    
  }
}
