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
  public static final IThreadState STOPPED = new ThreadStateStopped();
  /**
   * The thread we're managing is currently started.
   */
  public static final IThreadState STARTED = new ThreadStateStarted();
  /**
   * The thread we're managing is in the process of starting.
   */
  public static final IThreadState STARTING = new ThreadStateStarting();
  /**
   * The thread we're managing is in the process of stopping.
   */
  public static final IThreadState STOPPING = new ThreadStateStopping();
  

  /**
   * This event is fired when the object we're managing has successfully started.
   */
  public class RunnableStartedEvent extends Event
  {
    /**
     * Constructor for successful start.
     * @param source The object fired this event.
     */
    RunnableStartedEvent(Object source)
    {
      super(source);
    }
  }

  /**
   * This event is fired when the object we're managing has stopped.
   * 
   * If an error occurs while stopping, {@link RunnableStoppedEvent#getException()}
   * will return it.
   */
  public class RunnableStoppedEvent extends Event
  {
    private final Throwable mException;
   
    /**
     * Constructor for successful stop.
     * @param source The object fired this event.
     */
    RunnableStoppedEvent(Object source)
    {
      super(source);
      mException = null;
    }
    /**
     * Constructor for a stop with failure.
     * @param source The object that fired this event.
     * @param exception THe execption that caused the stoppage.
     */
    RunnableStoppedEvent(Object source, Throwable exception)
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
  public IThreadState getState();
}
