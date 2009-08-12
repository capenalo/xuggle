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

import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The standard implementation of {@link IAsynchronousEventDispatcher}.
 */
public class AsynchronousEventDispatcher 
extends SynchronousEventDispatcher
implements IAsynchronousEventDispatcher
{
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private Thread mDispatchThread;
  private final Queue<IEvent> mEventQueue;

  private final String mThreadName;

  /*
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder b = new StringBuilder();
    b.append(super.toString());
    b.append("[").append(mThreadName).append("];");
    return b.toString();
  }
  
  /**
   * Creates a {@link AsynchronousEventDispatcher}.
   * @param threadName The name to use for the thread started.
   * @param autoStartDispatching Should the constructor call {@link #startDispatching()}
   *   immediately.
   */
  public AsynchronousEventDispatcher(
      String threadName,
      boolean autoStartDispatching)
  {
    if (threadName == null)
      threadName = "DispatcherThread_"+this.hashCode();
    mThreadName = threadName;
    mDispatchThread = null;
    mEventQueue = new LinkedList<IEvent>();
    this.setupDispatching();
    if (autoStartDispatching)
      this.startDispatching();
  }
  /**
   * Create a new event dispatcher
   * @param autoStartDispatching If true, we start dispatching upon creation.  If false, we don't.
   */
  public AsynchronousEventDispatcher(
      boolean autoStartDispatching
      )
  {
    this(null, autoStartDispatching);
  }

  /**
   * Create a new object, but don't start dispatching automatically.
   * @param threadName the name to give the thread we start up.
   */
  public AsynchronousEventDispatcher(
      String threadName)
  {
    this(threadName, false);
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

    }, mThreadName);
    mDispatchThread.setDaemon(true);
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

  public void abortDispatching()
  {
    this.dispatchEvent(new EventDispatcherAbortEvent(this));
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

  public void dispatchEvent(IEvent event)
  {
    if (event == null)
      return;

    synchronized(this)
    {
      event.acquire();
      
      log.trace("dispatchEvent({})", event);
      // we need to queue the event.
      if (event instanceof EventDispatcherAbortEvent)
      {
        // clear out the queue and then add abort
        IEvent queueEvent;
        while((queueEvent = mEventQueue.poll()) != null)
        {
          queueEvent.release();
        }
        // Abort always jumps to the front of the queue
        log.debug("aborting dispatcher");
      }
      mEventQueue.offer(event);
      
      // and let the dispatch thread know
      this.notifyAll();
    }
  }

  private void runDispatcherThread()
  {
    boolean keepRunning = true;
    while (keepRunning)
    {
      int numPendingEvents = 0;
      IEvent pollEvent = null;
      synchronized(this)
      {
        while (pollEvent == null)
        {
          numPendingEvents = mEventQueue.size();
          pollEvent= mEventQueue.poll();
          if (pollEvent == null)
          {
            // wait for a notification
            try
            {
              this.wait();
            }
            catch(InterruptedException e)
            {
              pollEvent = new EventDispatcherAbortEvent(this);
            }
          }
        }
      }
      final IEvent event = pollEvent;

      if (event instanceof EventDispatcherStopEvent ||
          event instanceof EventDispatcherAbortEvent ||
          Thread.currentThread().isInterrupted())
      {
        synchronized(this)
        {
          log.debug("Got request to shut down");
          keepRunning = false;
          // empty the queue
          IEvent queueEvent;
          while((queueEvent = mEventQueue.poll()) != null)
            queueEvent.release();
        }
        event.release();
      } else {
        try
        {
          log.trace("pending events: {}; dispatchEvent({})", numPendingEvents,
              event);
          try
          {
            super.dispatchEvent(event);
          }
          finally
          {
            event.release();
          }
        } catch (Throwable t)
        {
          log.error("Dispatcher continuing after unhandled event: {}",
              t
              );
          t.printStackTrace();
        }
      }
    }
  }
}
