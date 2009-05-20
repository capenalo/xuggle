/*
 * This file is part of Xuggler.
 * 
 * Xuggler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Xuggler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Xuggler.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xuggle.utils.event.handler;

import com.xuggle.utils.event.IEvent;
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.IEventHandler;
import com.xuggle.utils.event.IEventHandlerRegistrable;

/**
 * A collection of convenience methods for registering handlers and
 * creating special handlers.
 * <p>
 * These are provided here because static generic function provide some
 * slight java compiler convenience.
 * </p>
 * @author aclarke
 *
 */

public class Handler
{

  private Handler()
  {
    // no handlers for you!
  }
  
  /**
   * Get a new {@link ForwardingHandler}.
   * @param dispatcherToForwardTo dispatcher for returned handler
   *   to forward events to
   * @return the new handler
   */

  public static ForwardingHandler getForwardingHandler(
      IEventDispatcher dispatcherToForwardTo)
  {
    return new ForwardingHandler(dispatcherToForwardTo);
  }


  /**
   * Get a new {@link BoundedHandler}
   * @param <E> class to handle
   * @param maxTimesToCall max times to call
   * @param proxiedHandler handler to proxy for
   * @return new handler
   */

  public static <E extends IEvent> BoundedHandler<E> getBoundedHandler(
      int maxTimesToCall,
      IEventHandler<E> proxiedHandler
  )
  {
    return new BoundedHandler<E>(
        maxTimesToCall,
        proxiedHandler);
  }

  /**
   * Get a new {@link TargetedHandler}
   * @param <E> class to handle
   * @param source this handler only forwards to proxiedHandler if
   *   {@link IEvent#getSource()} == source (or if source is null)
   * @param proxiedHandler the handler to proxy to
   * @return the handler
   */

  public static <E extends IEvent> TargetedHandler<E> getTargetedHandler(
      Object source,
      IEventHandler<E> proxiedHandler)
      {
    return new TargetedHandler<E>(source, proxiedHandler);
      }

  /**
   * Returns a {@link IEventHandler} that will only execute
   * if {@link IEvent#getSource()} == source and will also
   * only execute up to maxTimesToCall.
   * @param <E> Event class to handle.
   * @param source Source object to fire for.
   * @param maxTimesToCall Max times to fire.
   * @param proxiedHandler Proxied handler to fire
   * @return A handler you can use on {@link IEventHandler}
   */

  public static <E extends IEvent> IEventHandler<E> getTargetedAndBoundedHandler(
      Object source,
      int maxTimesToCall,
      IEventHandler<E> proxiedHandler)
      {
    BoundedHandler<E> bHandler = getBoundedHandler(maxTimesToCall,
        proxiedHandler);
    TargetedHandler<E> tHandler = getTargetedHandler(source, bHandler);
    return tHandler;
      }

  /**
   * Registers a handler on the registry.
   * @param <E> type of event
   * @param registry object to register handler on
   * @param priority priority
   * @param eventClass class of event; usually same as E
   * @param sourceToTarget if non null, your handler will only
   *   be called for events that have the same source.
   * @param maxTimesToCall if >0, your handler will only be called
   *   up to that number of times and will then be automatically
   *   removed.  If <= 0 the caller is responsible for removing.
   * @param proxiedHandler the handler you want to finally call
   * 
   * @return the handler we registered.
   */

  public static <E extends IEvent>
  IEventHandler<E> register(
      final IEventHandlerRegistrable registry,
      final int priority,
      final Class<? extends E> eventClass,
      final Object sourceToTarget,
      final int maxTimesToCall,
      final IEventHandler<E> proxiedHandler
  )
  {
    if (registry == null)
      throw new IllegalArgumentException("no registry");
    if (eventClass == null)
      throw new IllegalArgumentException("no event class");
    if (proxiedHandler == null)
      throw new IllegalArgumentException("no handler");

    final IEventHandler<E> boundedHandler;

    if (maxTimesToCall > 0)
    {
      boundedHandler = new BoundedHandler<E>(
          maxTimesToCall,
          proxiedHandler);
    } else
      boundedHandler = proxiedHandler;
    final IEventHandler<E> targetedHandler;
    if (sourceToTarget != null)
    {
      targetedHandler = new TargetedHandler<E>(
          sourceToTarget,
          boundedHandler);
    } else
      targetedHandler = boundedHandler;

    if (maxTimesToCall > 0)
    {
      BoundedHandler<E> bHandler = (BoundedHandler<E>)boundedHandler;
      bHandler.setLastEventHandler(new IEventHandler<E>(){
        public boolean handleEvent(IEventDispatcher dispatcher, E event)
        {
          try {
            registry.removeEventHandler(priority,
                eventClass,
                targetedHandler);
          } catch (IndexOutOfBoundsException e) {
            // if someone has already removed us, ignore it.
          }
          return false;
        }}
      );
    }
    registry.addEventHandler(priority, eventClass, targetedHandler);
    return targetedHandler;
  }

  /**
   * Register a handler on the registry that will fire regardless of
   * the event source.
   * @param <E> type of event
   * @param registry object to register handler on
   * @param priority priority
   * @param eventClass class of event; usually same as E
   * @param proxiedHandler the handler you want to finally call
   * @param maxTimesToCall if >0, your handler will only be called
   *   up to that number of times and will be auto-removed.  if <=0
   *   it is up to you to remove the handler.
   * @return the handler we registered.
   */

  public static <E extends IEvent>
  IEventHandler<E> register(
      final IEventHandlerRegistrable registry,
      final int priority,
      final Class<? extends E> eventClass,
      final int maxTimesToCall,
      final IEventHandler<E> proxiedHandler
  )
  {
    return register(registry,
        priority,
        eventClass,
        null,
        maxTimesToCall,
        proxiedHandler);
  }

  /**
   * Register a handler at priority 0 on the registry that will fire regardless of
   * the event source.
   * @param <E> type of event
   * @param registry object to register handler on
   * @param eventClass class of event; usually same as E
   * @param proxiedHandler the handler you want to finally call
   * @param maxTimesToCall if >0, your handler will only be called
   *   up to that number of times.
   * @return the handler we registered.
   */

  public static <E extends IEvent>
  IEventHandler<E> register(
      final IEventHandlerRegistrable registry,
      final Class<? extends E> eventClass,
      final int maxTimesToCall,
      final IEventHandler<E> proxiedHandler
  )
  {
    return register(registry,
        0,
        eventClass,
        null,
        maxTimesToCall,
        proxiedHandler);
  }


  /**
   * Registers a handler on the registry with a priority of 0.
   * @param <E> type of event
   * @param registry object to register handler on
   * @param eventClass class of event; usually same as E
   * @param sourceToTarget if non null, your handler will only
   *   be called for events that have the same source.
   * @param maxTimesToCall if >0, your handler will only be called
   *   up to that number of times.
   * @param proxiedHandler the handler you want to finally call
   */

  public static <E extends IEvent>
  IEventHandler<E> register(
      final IEventHandlerRegistrable registry,
      final Class<? extends E> eventClass,
      Object sourceToTarget,
      final int maxTimesToCall,
      final IEventHandler<E> proxiedHandler
  )
  {
    return register(registry,
        0,
        eventClass,
        sourceToTarget,
        maxTimesToCall,
        proxiedHandler
    );
  }

  /**
   * Registers a handler on the registry.  Caller is responsible
   * for removing.
   * @param <E> type of event
   * @param registry object to register handler on
   * @param priority priority
   * @param eventClass class of event; usually same as E
   * @param sourceToTarget if non null, your handler will only
   *   be called for events that have the same source.
   * @param proxiedHandler the handler you want to finally call
   * 
   * @return the handler we registered.
   */

  public static <E extends IEvent>
  IEventHandler<E> register(
      final IEventHandlerRegistrable registry,
      final int priority,
      final Class<? extends E> eventClass,
      final Object sourceToTarget,
      final IEventHandler<E> proxiedHandler
  )
  {
    return register(registry,
        priority,
        eventClass,
        sourceToTarget,
        0,
        proxiedHandler);
  }

  /**
   * Registers a handler with priority 0 on the registry.
   *   Caller is responsible for
   * removing.
   * @param <E> type of event
   * @param registry object to register handler on
   * @param eventClass class of event; usually same as E
   * @param sourceToTarget if non null, your handler will only
   *   be called for events that have the same source.
   * @param proxiedHandler the handler you want to finally call
   * 
   * @return the handler we registered.
   */


  public static <E extends IEvent>
  IEventHandler<E> register(
      final IEventHandlerRegistrable registry,
      final Class<? extends E> eventClass,
      Object sourceToTarget,
      final IEventHandler<E> proxiedHandler
  )
  {
    return register(registry,
        0,
        eventClass,
        sourceToTarget,
        0,
        proxiedHandler
    );
  }

  /**
   * Registers a handler on the registry.
   * Caller is responsible for removing.
   * @param <E> type of event
   * @param registry object to register handler on
   * @param priority priority
   * @param eventClass class of event; usually same as E
   * @param proxiedHandler the handler you want to finally call
   * 
   * @return the handler we registered.
   */

  public static <E extends IEvent>
  IEventHandler<E> register(
      final IEventHandlerRegistrable registry,
      final int priority,
      final Class<? extends E> eventClass,
      final IEventHandler<E> proxiedHandler
  )
  {
    return register(registry,
        priority,
        eventClass,
        null,
        0,
        proxiedHandler);
  }

  /**
   * Registers a handler with priority 0 on the registry.
   * Caller is responsible for removing.
   * @param <E> type of event
   * @param registry object to register handler on
   * @param eventClass class of event; usually same as E
   * @param proxiedHandler the handler you want to finally call
   * 
   * @return the handler we registered.
   */
  public static <E extends IEvent>
  IEventHandler<E> register(
      final IEventHandlerRegistrable registry,
      final Class<? extends E> eventClass,
      final IEventHandler<E> proxiedHandler
  )
  {
    return register(registry,
        0,
        eventClass,
        null,
        0,
        proxiedHandler);
  }


}
