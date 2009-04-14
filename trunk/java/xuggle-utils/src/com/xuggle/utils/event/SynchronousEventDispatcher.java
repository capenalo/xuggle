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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
  final private Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * Here's the data structure type
   * 
   *   A map of class names to:
   *      A Map (because it can be sparse) of Priority to a list of Event Handlers
   *        A list of event Handlers
   */

  private final Map<String, SortedMap<Integer, List<IEventHandler>>> mHandlers;
  
  private final AtomicLong mNumNestedEventDispatches;
  private final Queue<IEvent> mPendingEventDispatches;

  private abstract class EventHandlerEvent extends SelfHandlingEvent
  
  {
    private final int mPriority;
    private final Class<? extends IEvent> mEventClass;
    private final IEventHandler mHandler;
    
    public EventHandlerEvent(
        SynchronousEventDispatcher dispatcher,
        int priority,
        Class<? extends IEvent> eventClass,
        IEventHandler handler)
    {
      super(dispatcher);
      mPriority = priority;
      mEventClass = eventClass;
      mHandler = handler;
      log.trace("<init>");
    }
    
    
    public SynchronousEventDispatcher getSource()
    {
      return (SynchronousEventDispatcher)super.getSource();
    }

    public IEventHandler getHandler()
    {
      return mHandler;
    }

    public Class<? extends IEvent> getEventClass()
    {
      return mEventClass;
    }

    public int getPriority()
    {
      return mPriority;
    }

    
    public abstract boolean handleEvent(IEventDispatcher aDispatcher, IEvent aEvent);
  }
  private class AddEventHandlerEvent extends EventHandlerEvent
  {
    public AddEventHandlerEvent(SynchronousEventDispatcher aDispatcher, int aPriority,
        Class<? extends IEvent> aEventClass, IEventHandler aHandler)
    {
      super(aDispatcher, aPriority, aEventClass, aHandler);
    }

    
    public boolean handleEvent(IEventDispatcher aDispatcher, IEvent aEvent)
    {
      getSource().dispatchedAddEventHandler(getPriority(), getEventClass(), getHandler());
      return false;
    }    
  }
  private class RemoveEventHandlerEvent extends EventHandlerEvent
  {
    public RemoveEventHandlerEvent(SynchronousEventDispatcher aDispatcher, int aPriority,
        Class<? extends IEvent> aEventClass, IEventHandler aHandler)
    {
      super(aDispatcher, aPriority, aEventClass, aHandler);
    }

    
    public boolean handleEvent(IEventDispatcher aDispatcher, IEvent aEvent)
    {
      getSource().dispatchedRemoveEventHandler(getPriority(), getEventClass(), getHandler());
      return false;
    } 
  }
  public SynchronousEventDispatcher()
  {
    mNumNestedEventDispatches = new AtomicLong(0);
    mPendingEventDispatches = new ConcurrentLinkedQueue<IEvent>();
    mHandlers = new HashMap<String, SortedMap<Integer,List<IEventHandler>>>();
  }
  
  private void dispatchedAddEventHandler(int priority,
      Class<? extends IEvent> eventClass,
      IEventHandler handler)
  {
    if (eventClass == null)
      throw new IllegalArgumentException("cannot pass null class");
    if (handler == null)
      throw new IllegalArgumentException("cannot pass null handler");
    
    String className = eventClass.getName();
    if (className == null || className.length() <= 0)
      throw new IllegalArgumentException("cannot get name of class");
    
    SortedMap<Integer, List<IEventHandler>> priorities = mHandlers.get(className);
    if (priorities == null)
    {
      priorities = new TreeMap<Integer, List<IEventHandler>>();
      mHandlers.put(className, priorities);
    }
    
    List<IEventHandler> handlers = priorities.get(priority);
    if (handlers == null)
    {
      handlers = new ArrayList<IEventHandler>();
      priorities.put(priority, handlers);
    }
    handlers.add(handler);
    // and we're done.
  }

  public void dispatchEvent(IEvent event)
  {
    long dispatcherNum = mNumNestedEventDispatches.incrementAndGet();
    try
    {
      if (event == null)
        throw new IllegalArgumentException("cannot dispatch null event");
      
      //log.debug("dispatching event: {}", event);
      mPendingEventDispatches.add(event);
      // don't process a dispatch if nested within a dispatchEvent() call;
      // wait for the stack to unwind, and then process it.
      while(dispatcherNum == 1
          && (event = mPendingEventDispatches.poll()) != null)
      {
        boolean eventHandled = false;
        Queue<IEventHandler> handlers = new LinkedList<IEventHandler>();
        
        // First, determine all the valid handlers
        
        // self handlers ARE ALWAYS called first
        if (event instanceof ISelfHandlingEvent)
          handlers.add((IEventHandler)event);
                
        // find our registered handlers.
        String className = event.getClass().getName();
        if (className == null)
          throw new IllegalArgumentException("cannot get class name for event");

        Map<Integer, List<IEventHandler>> priorities = mHandlers.get(className);
        if (priorities != null)
        {
          Set<Integer> priorityKeys = priorities.keySet();
          Iterator<Integer> orderedKeys = priorityKeys.iterator();
          while(orderedKeys.hasNext())
          {
            Integer priority = orderedKeys.next();
            List<IEventHandler> priHandlers = priorities.get(priority);
            if (priHandlers != null)
            {
              handlers.addAll(priHandlers);
            }
          }
        }
        //log.debug("Handling event: {} with {} handlers", event, handlers.size());
        Iterator<IEventHandler> handlersIter = handlers.iterator();
        while(!eventHandled && handlersIter.hasNext())
        {
          IEventHandler handler = handlersIter.next();
          //log.debug("Handling event: {} with handler: {}", event, handler);
          eventHandled = handler.handleEvent(this, event);
        }
        //log.debug("Handling event: {} done", event);
      }
    }
    finally
    {
      mNumNestedEventDispatches.decrementAndGet();
    }
  }

  private void dispatchedRemoveEventHandler(int priority,
      Class<? extends IEvent> eventClass,
      IEventHandler handler) throws IndexOutOfBoundsException
  {
    if (eventClass == null)
      throw new IllegalArgumentException("cannot pass null class");
    if (handler == null)
      throw new IllegalArgumentException("cannot pass null handler");
    
    String className = eventClass.getName();
    if (className == null || className.length() <= 0)
      throw new IllegalArgumentException("cannot get name of class");
    
    Map<Integer, List<IEventHandler>> priorities = mHandlers.get(className);
    if (priorities == null)
    {
      // could not find entry in list
      throw new IndexOutOfBoundsException();
    }
    
    List<IEventHandler> handlers = priorities.get(priority);
    if (handlers == null)
    {
      // could not find entry in list
      throw new IndexOutOfBoundsException();
    }

    ListIterator<IEventHandler> iter = handlers.listIterator();
    // Walk through and remove all copies of this handler.
    // someone may have registered multiple copies of the same
    // handler, and we nuke them all
    int handlersNuked = 0;
    while(iter.hasNext())
    {
      IEventHandler registeredHandler = iter.next();
      if (registeredHandler == handler)
      {
        iter.remove();
        ++handlersNuked;
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

  public void addEventHandler(int aPriority,
      Class<? extends IEvent> aEventClass, IEventHandler aHandler)
  {
    this.dispatchEvent(new AddEventHandlerEvent(this, aPriority, aEventClass, aHandler));
  }

  public void removeEventHandler(int aPriority,
      Class<? extends IEvent> aEventClass, IEventHandler aHandler)
      throws IndexOutOfBoundsException
  {
    this.dispatchEvent(new RemoveEventHandlerEvent(this, aPriority, aEventClass, aHandler));
  }
}
