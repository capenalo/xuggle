package com.xuggle.utils.event.handler;

import static org.junit.Assert.*;

import org.junit.Test;

import com.xuggle.utils.event.Event;
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.MockNullEventHandler;
import com.xuggle.utils.event.SynchronousEventDispatcher;

public class BoundedHandlerTest
{
  private class TestEvent extends Event
  {
    public TestEvent(Object source) { super(source); }
  }

  @Test
  public final void testBoundedHandler()
  {
    try
    {
      new BoundedHandler<TestEvent>(
          null,
          0,
          TestEvent.class,
          null,
          -1);
      fail("should not get here"); 
    }catch(IllegalArgumentException e){}
    try
    {
      new BoundedHandler<TestEvent>(
          null,
          0,
          null,
          new MockNullEventHandler<TestEvent>(),
          -1);
      fail("should not get here"); 
    }catch(IllegalArgumentException e){}
    new BoundedHandler<TestEvent>(
        null,
        0,
        TestEvent.class,
        new MockNullEventHandler<TestEvent>(),
        -1);
  }

  @Test
  public final void testHandleEvent()
  {
    IEventDispatcher dispatcher = new SynchronousEventDispatcher();
    Object source = new Object();
    TestEvent event = new TestEvent(source);

    int maxCalls = 4;
    BoundedHandler<TestEvent> handler = Handlers.getBoundedHandler(
        dispatcher,
        0,
        TestEvent.class,
        new MockNullEventHandler<TestEvent>(),
        maxCalls
        );

    for(int i = 0; i < maxCalls; i++)
    {
      assertEquals(i, handler.getNumTimesExecuted());
      dispatcher.dispatchEvent(event);
      assertEquals(i+1, handler.getNumTimesExecuted());
    }
    // now should no longer execute
    for(int i = 0; i < 5; i++)
    {
      assertEquals(maxCalls, handler.getNumTimesExecuted());
      dispatcher.dispatchEvent(event);
      assertEquals(maxCalls, handler.getNumTimesExecuted());
    }
  }

}
