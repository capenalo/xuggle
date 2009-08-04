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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A synchronous implementation of {@link IEventDispatcher}.  This
 * implementation is not thread safe; i.e. if multiple threads try
 * dispatching at the same time, correctness is not guaranteed.
 * 
 * <p>
 * 
 * This method will guarantee that another event will not be dispatched
 * until all handlers for the current event being dispatched have been called
 * and returned.
 * 
 * </p>
 * <p>
 * 
 * This means that, even if a handler causes another event to be dispatched,
 * the new event will be queued until the current handler completely
 * unwinds.
 * 
 * </p> 
 */

public class SynchronousEventDispatcher implements IEventDispatcher
{
  private static class HandlerReference
  extends WeakReference<IEventHandler<? extends IEvent>>
  implements Key
  {
    private final Class<? extends IEvent> mClass;
    private final int mPriority;
    private final IEventHandler<? extends IEvent> mHandler;
    public HandlerReference(IEventHandler<? extends IEvent> referent,
        ReferenceQueue<IEventHandler<? extends IEvent>> q,
            int priority, Class<? extends IEvent> clazz,
            boolean useWeakReference
    )
    {
      super(referent, q);
      mClass = clazz;
      mPriority = priority;
      if (useWeakReference)
        mHandler = null;
      else
        mHandler = referent;
      
    }

    public Class<? extends IEvent> getEventClass()
    {
      return mClass;
    }

    public int getPriority()
    {
      return mPriority;
    }

    public IEventHandler<? extends IEvent> getHandler()
    {
      return mHandler;
    }
  }
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * Here's the data structure type
   * 
   *   A map of class names to:
   *      A Map (because it can be sparse) of Priority to a list of Event Handler
   *        A list of event Handler
   */

  private final Map<String, SortedMap<Integer,
  List<HandlerReference>>> mHandlers;
  
  private final AtomicLong mNumNestedEventDispatches;
  private final Queue<IEvent> mPendingEventDispatches;

  private ReferenceQueue<IEventHandler<? extends IEvent>> mReferenceQueue;

  public SynchronousEventDispatcher()
  {
    mNumNestedEventDispatches = new AtomicLong(0);
    mPendingEventDispatches = new ConcurrentLinkedQueue<IEvent>();
    mHandlers = new HashMap<String, SortedMap<Integer,
      List<HandlerReference>>>();
    mReferenceQueue = new ReferenceQueue<IEventHandler<? extends IEvent>>();
    log.trace("<init>");
  }
  
  public Key addEventHandler(final int priority,
      final Class<? extends IEvent> eventClass,
      final IEventHandler<? extends IEvent> handler)
  {
    return addEventHandler(priority, eventClass, handler, false);
  }
  public Key addEventHandler(final int priority,
      final Class<? extends IEvent> eventClass,
      final IEventHandler<? extends IEvent> handler,
      final boolean useWeakReferences)
  {
    if (eventClass == null)
      throw new IllegalArgumentException("cannot pass null class");
    if (handler == null)
      throw new IllegalArgumentException("cannot pass null handler");
    
    final String className = eventClass.getName();
    if (className == null || className.length() <= 0)
      throw new IllegalArgumentException("cannot get name of class");
   
    final HandlerReference reference = new HandlerReference(
        handler, 
        mReferenceQueue,
        priority,
        eventClass,
        useWeakReferences);
    synchronized(mHandlers)
    {
      SortedMap<Integer, List<HandlerReference>> priorities
        = mHandlers.get(className);
      if (priorities == null)
      {
        priorities = new TreeMap<Integer, List<HandlerReference>>();
        mHandlers.put(className, priorities);
      }

      List<HandlerReference> handlers = priorities.get(priority);
      if (handlers == null)
      {
        handlers = new ArrayList<HandlerReference>();
        priorities.put(priority, handlers);
      }
      handlers.add(reference);
      // and we're done.
    }
    
    // now outside the lock, communicate what just happened
    dispatchEvent(new EventHandlerAddedEvent(this, reference,
        priority, eventClass, handler,
        useWeakReferences));
    return reference;
  }

  // This is cached here so that we don't constantly reallocate
  // the array; but it will grow unbounded to the the length of
  // the event that has the most handlers to call.
  private IEventHandler<?extends IEvent>[] mHandlersToCall = null;
  
  @SuppressWarnings("unchecked")
  public void dispatchEvent(IEvent event)
  {
    long dispatcherNum = mNumNestedEventDispatches.incrementAndGet();
    final IEvent origEvent = event;
    try
    {
      if (event == null)
        throw new IllegalArgumentException("cannot dispatch null event");
      
      //log.debug("dispatching event: {}", event);
      // do one acquire for adding to the queue
      origEvent.acquire();
      mPendingEventDispatches.add(origEvent);
      // don't process a dispatch if nested within a dispatchEvent() call;
      // wait for the stack to unwind, and then process it.
      while(!Thread.currentThread().isInterrupted() &&
          dispatcherNum == 1 &&
          (event = mPendingEventDispatches.poll()) != null)
      {
        boolean eventHandled = false;
        // always reset the numHandler
        int numHandlers = 0;
        
        // First, determine all the valid handlers
        
        // find our registered handlers.
        final String className = event.getClass().getName();
        if (className == null)
          throw new IllegalArgumentException("cannot get class name for event");

        // self handlers ARE ALWAYS called first
        if (event instanceof ISelfHandlingEvent)
        {
          ISelfHandlingEvent<? extends IEvent> selfHandlingEvent =
            (ISelfHandlingEvent<? extends IEvent>)event;
          mHandlersToCall = addHandler(mHandlersToCall, selfHandlingEvent, numHandlers);
          numHandlers++;
        }
        synchronized(mHandlers)
        {
          Map<Integer, List<HandlerReference>> priorities
            = mHandlers.get(className);
          if (priorities != null)
          {
            final Set<Integer> priorityKeys = priorities.keySet();
            final Iterator<Integer> orderedKeys = priorityKeys.iterator();
            while(orderedKeys.hasNext())
            {
              final Integer priority = orderedKeys.next();
              final List<HandlerReference> priHandlers
                = priorities.get(priority);
              if (priHandlers != null)
              {
                for(HandlerReference reference : priHandlers)
                {
                  final IEventHandler<? extends IEvent> handler = reference.get();
                  if (handler != null) {
                    mHandlersToCall = addHandler(mHandlersToCall, handler, numHandlers);
                    numHandlers++;
                  }
                }
              }
            }
          }
        }
        log.trace("Handling event: {} with {} handlers", event, numHandlers);
        int i = 0;
        try {
          for(i = 0; i < numHandlers; i++) {
            final IEventHandler handler = mHandlersToCall[i];
            mHandlersToCall[i] = null;
            if (eventHandled)
              break;
            if (Thread.currentThread().isInterrupted())
              break;
            if (handler == null)
              break;
            //log.debug("Handling event: {} with handler: {}", event, handler);
            try {
              eventHandled = handler.handleEvent(this, event);
            }
            catch (AssertionError t) {
              // to enable tests to continue working, we dispatch an event but
              // rethrow.
              dispatchEvent(new ErrorEvent(event.getSource(),
                  t,
                  "uncaught exception",
                  event,
                  handler));
              throw t;
            }
            catch (Throwable t)
            {
              dispatchEvent(new ErrorEvent(event.getSource(),
                  t,
                  "uncaught exception",
                  event,
                  handler));
            }
          }
        } finally {
          // clear out any handlers we didn't clear above 
          for(; i < numHandlers; i++)
            mHandlersToCall[i] = null;
        }
        // do one release for finishing the handle
        event.release();
        //log.debug("Handling event: {} done", event);
        
        // and finish by checking our reference queue and removing any event
        // handlers that are now dead.
        HandlerReference deadRef;
        while((deadRef = (HandlerReference)mReferenceQueue.poll()) != null)
          removeDeadHandler(deadRef);
      }
    }
    finally
    {
      mNumNestedEventDispatches.decrementAndGet();
    }
  }

  private IEventHandler<? extends IEvent>[] addHandler(
      IEventHandler<? extends IEvent>[] handlers,
      IEventHandler<? extends IEvent> handler,
      int numHandlers)
  {
    if (handlers == null) {
      handlers = new IEventHandler<?>[10];
    }
    if (numHandlers >= handlers.length)
    {
      // double the array in size
      IEventHandler<? extends IEvent>[] newHandlers = new IEventHandler<?>[handlers.length*2];
      System.arraycopy(handlers, 0, newHandlers, 0, numHandlers);
      handlers = newHandlers;
    }
    handlers[numHandlers] = handler;
    return handlers;
  }

  private void removeDeadHandler(final HandlerReference deadRef)
  {
    final Class<? extends IEvent> eventClass = deadRef.getEventClass();
    final int priority = deadRef.getPriority();
    
    if (eventClass == null)
      throw new IllegalArgumentException("cannot pass null class");

    final String className = eventClass.getName();
    if (className == null || className.length() <= 0)
      throw new IllegalArgumentException("cannot get name of class");
    synchronized(mHandlers)
    {
      final Map<Integer, List<HandlerReference>> priorities
        = mHandlers.get(className);
      if (priorities == null)
      {
        // could not find entry in list
        return;
      }

      final List<HandlerReference> handlers = priorities.get(priority);
      if (handlers == null)
      {
        // could not find entry in list
        return;
      }

      final ListIterator<HandlerReference> iter = handlers.listIterator();
      while(iter.hasNext())
      {
        final HandlerReference registeredHandlerReference = iter.next();
        if (registeredHandlerReference == deadRef)
        {
          iter.remove();
        }
      }
      if (handlers.size() ==0)
      {
        // All handlers were removed for this priority; clean up.
        priorities.remove(priority);
      }
      if (priorities.size() == 0)
      {
        // all priorities were removed for this class; clean up
        mHandlers.remove(className);
      }
    }
    // now outside the lock, communicate what just happened
    dispatchEvent(new EventHandlerRemovedEvent(this, deadRef,
        priority, eventClass,
        null));

  }

  public void removeEventHandler(final Key key) throws IndexOutOfBoundsException
  {
    if (!(key instanceof HandlerReference))
      throw new IndexOutOfBoundsException("key was not gotten from addEventHandler");
    
    final HandlerReference reference = (HandlerReference) key;
    
    final Class<? extends IEvent> eventClass = reference.getEventClass();
    if (eventClass == null)
      throw new IllegalArgumentException("cannot pass null class");
    
    final IEventHandler<? extends IEvent> handler = reference.getHandler();
    if (handler == null)
      throw new IllegalArgumentException("cannot pass null handler");
    
    final String className = eventClass.getName();
    if (className == null || className.length() <= 0)
      throw new IllegalArgumentException("cannot get name of class");
    
    final int priority = reference.getPriority();
    synchronized(mHandlers)
    {
      final Map<Integer, List<HandlerReference>> priorities
        = mHandlers.get(className);
      if (priorities == null)
      {
        // could not find entry in list
        throw new IndexOutOfBoundsException();
      }

      final List<HandlerReference> handlers = priorities.get(priority);
      if (handlers == null)
      {
        // could not find entry in list
        throw new IndexOutOfBoundsException();
      }

      final ListIterator<HandlerReference> iter = handlers.listIterator();
      // Walk through and remove all copies of this handler.
      // someone may have registered multiple copies of the same
      // handler, and we nuke them all
      int handlersNuked = 0;
      while(iter.hasNext())
      {
        final HandlerReference registeredHandlerReference = iter.next();
        final IEventHandler<? extends IEvent> registeredHandler = 
          registeredHandlerReference.get();
        if (registeredHandler == handler)
        {
          iter.remove();
          ++handlersNuked;
          // and we're done; break the loop
          break;
        }
      }
      if (handlersNuked == 0)
      {
        // could not find entry in list
        throw new IndexOutOfBoundsException();      
      }
      if (handlers.size() ==0)
      {
        // All handlers were removed for this priority; clean up.
        priorities.remove(priority);
      }
      if (priorities.size() == 0)
      {
        // all priorities were removed for this class; clean up
        mHandlers.remove(className);
      }
    }
    // now outside the lock, communicate what just happened
    dispatchEvent(new EventHandlerRemovedEvent(this, reference, priority, eventClass, handler));

  }

}
