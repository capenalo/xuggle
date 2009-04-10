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

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The standard implementation of {@link IAsynchronousEventDispatcher}.
 */
public class AsynchronousEventDispatcher implements IAsynchronousEventDispatcher
{
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private Thread mDispatchThread;
  private LinkedList<IEvent> mEventQueue;
  private SynchronousEventDispatcher mDispatcher;

  /**
   * Create a new event dispatcher
   * @param autoStartDispatching If true, we start dispatching upon creation.  If false, we don't.
   */
  public AsynchronousEventDispatcher(
      boolean autoStartDispatching
      )
  {
    mDispatchThread = null;
    mEventQueue = new LinkedList<IEvent>();
    mDispatcher = new SynchronousEventDispatcher();
    this.setupDispatching();
    if (autoStartDispatching)
      this.startDispatching();

  }

  /**
   * Create a new object, but don't start dispatching automatically.
   */
  public AsynchronousEventDispatcher()
  {
    this(false);
  }

  private void setupDispatching()
  {
    mDispatchThread = new Thread(new Runnable(){
      public void run()
      {
        runDispatcherThread();
      }

    }, "DispatcherThread");
  }

  public synchronized void startDispatching()
  {
    if (mDispatchThread == null ||
        mDispatchThread.getState() == Thread.State.TERMINATED)
    {
      this.setupDispatching();
    }

    if (mDispatchThread.isAlive())
      return;

    assert mDispatchThread != null : "Error; could not create thread";
    mDispatchThread.start();
  }

  public void stopDispatching()
  {
    this.dispatchEvent(new EventDispatcherStopEvent(this));
  }

  public void waitForDispatcherToFinish(long timeout)
  {
    Thread dispatchThread = null;

    // in general a stopDispatching shouldn't lock,
    // but since we're going to wait, we want to avoid the race
    // where the dispatching thread actually stops and nukes the
    // mDispatchThread variable.  So we grab it here, and then
    // use our local reference for the join (which should return
    // immediately if the thread is already dead).
    synchronized (this)
    {
      if (mDispatchThread.isAlive())
        dispatchThread = mDispatchThread;
    }
    if (dispatchThread != null)
    {
      try
      {
        dispatchThread.join(timeout);
      }
      catch (InterruptedException e)
      {
      }
    }
  }
  
  public boolean isDispatching()
  {
    return mDispatchThread.isAlive();
  }

  public void addEventHandler(int priority, Class<? extends IEvent> eventClass, IEventHandler handler)
  {
    synchronized(this)
    {
      mDispatcher.addEventHandler(priority, eventClass, handler);
    }
  }

  public void dispatchEvent(IEvent event)
  {
    if (event == null)
      return;

    synchronized(this)
    {
      // we need to queue the event.
      if (event instanceof EventDispatcherAbortEvent)
      {
        // Abort always jumps to the front of the queue
        log.debug("aborting dispatcher");
        mEventQueue.addFirst(event);
      } else {
        mEventQueue.addLast(event);
      }
      // and let the dispatch thread know
      this.notifyAll();
    }
  }

  public synchronized void removeEventHandler(int priority,
      Class<? extends IEvent> eventClass,
      IEventHandler handler) throws IndexOutOfBoundsException
  {
    synchronized(this)
    {
      mDispatcher.removeEventHandler(priority, eventClass, handler);
    }
  }

  private void runDispatcherThread()
  {
    boolean keepRunning = true;
    while (keepRunning)
    {
      IEvent event = null;
      while(event == null)
      {
        synchronized(this)
        {
          event = mEventQueue.poll();
          if (event == null)
          {
            // wait for a notification
            try
            {
              this.wait();
            }
            catch(InterruptedException e)
            {
              event = new EventDispatcherAbortEvent(this);
            }
          }
        }
      }

      if (event instanceof EventDispatcherStopEvent ||
          event instanceof EventDispatcherAbortEvent)
      {
        synchronized(this)
        {
          log.debug("Got request to shut down");
          keepRunning = false;
          // empty the queue
          mEventQueue.clear();
        }
      } else {
        mDispatcher.dispatchEvent(event);
      }
    }
  }
}
