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
package com.xuggle.utils.tlm;

import com.xuggle.utils.event.Event;

/**
 * A {@link IThreadLifecycleManager} manages the lifecycle of another object on a separate thread.
 * <p>
 * It ensures that the other objects has a chance to initialize before the thread is consider
 * to have {@link #STARTED}.
 * </p>
 * @see IThreadLifecycleManagedRunnable
 */
public interface IThreadLifecycleManager
{

  // The states we can be in.
  

  /**
   * The thread we're managing is currently stopped. 
   */
  public static final ThreadState STOPPED = new ThreadStateStopped();
  /**
   * The thread we're managing is currently started.
   */
  public static final ThreadState STARTED = new ThreadStateStarted();
  /**
   * The thread we're managing is in the process of starting.
   */
  public static final ThreadState STARTING = new ThreadStateStarting();
  /**
   * The thread we're managing is in the process of stopping.
   */
  public static final ThreadState STOPPING = new ThreadStateStopping();
  

  /**
   * This event is fired when the object we're managing has successfully started.
   */
  public class RunnableStarted extends Event
  {
    /**
     * Constructor for successful start.
     * @param source The object fired this event.
     */
    public RunnableStarted(Object source)
    {
      super(source);
    }
  }

  /**
   * This event is fired when the object we're managing has stopped.
   * 
   * If an error occurs while stopping, {@link RunnableStopped#getException()}
   * will return it.
   */
  public class RunnableStopped extends Event
  {
    private final Throwable mException;
   
    /**
     * Constructor for successful stop.
     * @param source The object fired this event.
     */
    public RunnableStopped(Object source)
    {
      super(source);
      mException = null;
    }
    /**
     * Constructor for a stop with failure.
     * @param source The object that fired this event.
     * @param exception THe execption that caused the stoppage.
     */
    public RunnableStopped(Object source, Throwable exception)
    {
      super(source);
      mException = exception;
    }
    
    /**
     * Any exception that caused the thread being managed to stop.
     * @return an exception if it caused the thread to stop, else null.
     */
    public Throwable getException()
    {
      return mException;
    }
  }
  
  /**
   * Attempts to start the managed object and immediately returns.
   */
  public void start();
  
  /**
   * Calls {@link #start()} and then waits for the thread
   * state to enter the {@link #STARTED} state.
   * 
   * @param waitTimeout Time in milliseconds to wait, or 0 to wait indefinitely
   */
  public void startAndWait(long waitTimeout);
  
  /**
   * Attempts to stop the worker if STARTED and
   * immediately returns.
   */
  public void stop();

  /**
   * Calls {@link #stop()} and then waits for the thread
   * state to enter the {@link #STOPPED} state.
   * 
   * @param waitTimeout Time in milliseconds to wait, or 0 to wait indefinitely
   */
  public void stopAndWait(long waitTimeout);
  
  /**
   * Gets the current state of the thread we're managing
   * <p/>
   * Note: Every time the state changes, the manager
   * will notify itself.
   * <p/>
   * So for example if you're waiting for a thread
   * to enter a {@link #STOPPED} state, you can do:
   * <code>
   * synchronized(manager)
   * {
   *   while(manager.getState() != ThreadState.STOPPED)
   *    manager.wait();
   * }
   * </code>
   * <p/>
   * @return state of worker thread
   */
  public ThreadState getState();
}
