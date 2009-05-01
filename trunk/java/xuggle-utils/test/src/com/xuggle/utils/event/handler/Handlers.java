package com.xuggle.utils.event.handler;

import com.xuggle.utils.event.IEvent;
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.IEventHandler;
import com.xuggle.utils.event.IEventHandlerRegistrable;

/**
 * A collection of factory methods for special handlers.
 * @author aclarke
 *
 */
public class Handlers
{
  
  public static ForwardingHandler getForwardingHandler(
      IEventDispatcher dispatcherToForwardTo)
  {
    return new ForwardingHandler(dispatcherToForwardTo);
  }
  
  public static <E extends IEvent> BoundedHandler<E> getBoundedHandler(
      IEventHandlerRegistrable registry,
      int priority,
      Class<E> eventClass,
      IEventHandler<E> eventHandler,
      int maxTimesToCall
  )
  {
    return new BoundedHandler<E>(registry, priority, eventClass,
        eventHandler, maxTimesToCall);
  }
  public static <E extends IEvent> BoundedHandler<E> getBoundedHandler(
      IEventHandlerRegistrable registry,
      Class<E> eventClass,
      IEventHandler<E> eventHandler,
      int maxTimesToCall
  )
  {
    return new BoundedHandler<E>(registry, eventClass,
        eventHandler, maxTimesToCall);
  }
  public static <E extends IEvent> BoundedHandler<E> getBoundedHandler(
      Class<E> eventClass,
      IEventHandler<E> eventHandler,
      int maxTimesToCall
  )
  {
    return new BoundedHandler<E>(eventClass,
        eventHandler, maxTimesToCall);
  }

  public static <E extends IEvent> TargetedHandler<E> getTargetedHandler(
      Object source,
      IEventHandler<E> handler)
  {
    return new TargetedHandler<E>(source, handler);
  }

}
