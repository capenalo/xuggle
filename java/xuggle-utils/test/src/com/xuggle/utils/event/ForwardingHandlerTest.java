package com.xuggle.utils.event;

import static org.junit.Assert.*;

import org.junit.Test;

import com.xuggle.utils.Mutable;

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
