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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.xuggle.utils.event.Event;
import com.xuggle.utils.event.IAsynchronousEventDispatcher;
import com.xuggle.utils.event.IEvent;
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.IEventHandler;
import com.xuggle.utils.sm.StateMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link IThreadLifecycleManager} that uses a {@link StateMachine} to manage the state.
 */
public class ThreadLifecycleManager extends StateMachine
  implements IEventDispatcher, IThreadLifecycleManager
{
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * The ExecutorService we use to launch worker threads
   */
  private ExecutorService mExecutor=null;
  private ExecutorService mInternalExecutor=null;
  
  /**
   * The object we're managing on the separate thread
   */
  private IThreadLifecycleManagedRunnable mManagedObject = null;
  private long mNumStarting=0;
  private long mNumStarted=0;
  private long mNumStopping=0;
  private long mNumStopped=0;

  private Throwable mWorkerException = null;
  
  private abstract class InternalEvent extends Event implements IEventHandler
  {
    InternalEvent(IThreadLifecycleManager sm)
    {
      super(sm);
    }
    
    public ThreadLifecycleManager getSource()
    {
      return (ThreadLifecycleManager)super.getSource();
    }

    public abstract boolean handleEvent(IEventDispatcher dispatcher, IEvent event);
    
  }
  
  class StartEvent extends InternalEvent
  {
    public StartEvent(IThreadLifecycleManager sm)
    {
      super(sm);
    }

    @Override
    public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
    {
      getSource().getState().start(getSource());
      return false;
    }
  }

  class StartedEvent extends InternalEvent
  {
    public StartedEvent(IThreadLifecycleManager sm)
    {
      super(sm);
    }

    @Override
    public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
    {
      getSource().getState().onStarted(getSource());
      return false;
    }
  }
  
  class StopEvent extends InternalEvent
  {
    public StopEvent(IThreadLifecycleManager sm)
    {
      super(sm);
    }

    @Override
    public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
    {
      getSource().getState().stop(getSource());
      return false;
    }
  }

  class StoppedEvent extends InternalEvent
  {
    private final Throwable mException;
    public StoppedEvent(IThreadLifecycleManager sm, Throwable t)
    {
      super(sm);
      mException = t;
    }

    @Override
    public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
    {
      getSource().getState().onStopped(getSource(), getException());
      return false;
    }

    public Throwable getException()
    {
      return mException;
    }
  }

  /**
   * Constructs a manager without a managed object.
   * <b>
   * You must call {@link #setManagedObject(IThreadLifecycleManagedRunnable)}
   * with a non-null object before calling any other method
   * </b>
   */
  public ThreadLifecycleManager()
  {
    this(null, null, null);
  }
  
  /**
   * Constructs a manager with a managed object.
   * @param managedObject The object to manage.
   */
  public ThreadLifecycleManager(
      IThreadLifecycleManagedRunnable managedObject
      )
  {
    this(managedObject, null, null);
  }
  
  /**
   * Constructs a manager with a managed object.
   * @param managedObject The object to manage.
   * @param eventDispatcher An event dispatcher to use.
   */
  public ThreadLifecycleManager(
      IThreadLifecycleManagedRunnable managedObject,
      IAsynchronousEventDispatcher eventDispatcher
      )
  {
    this(managedObject, eventDispatcher, null);
  }
  
  /**
   * Constructs a manager with a managed object.
   * @param managedObject The object to manage.
   * @param eventDispatcher An event dispatcher to use.
   * @param executor An executor to get new threads from.
   */
  public ThreadLifecycleManager(
      IThreadLifecycleManagedRunnable managedObject,
      IAsynchronousEventDispatcher eventDispatcher,
      ExecutorService executor
      )
  {
    super(eventDispatcher, STOPPED);

    mManagedObject = managedObject; // allows null for now
    mExecutor = executor;
    if (mExecutor == null) {
      mInternalExecutor = Executors.newFixedThreadPool(1);
      log.debug("Creating our own executor for threads: {}",
          mInternalExecutor);
      mExecutor = mInternalExecutor;
    }
    // register our handlers
    
    // IBroadcaster main handlers
    this.addEventHandler(0, StartEvent.class, this);
    this.addEventHandler(0, StartedEvent.class, this);
    this.addEventHandler(0, StopEvent.class, this);
    this.addEventHandler(0, StoppedEvent.class, this);
    log.debug("created");
 
  }

  /**
   * Sets the managed object for this manager.  You can only call
   * this if you passed null for a managed object into a constructor,
   * and you can only call this once.
   * <p>
   * It exists so that you can construct a manager, but have a worker
   * as an inner class.
   * </p>
   * <p>
   * Really, you're better off just passing a managed object into a constructor
   * like a good boy or girl.
   * </p> 
   * @param aManagedObject The object you want to manage
   */
  public void setManagedObject(IThreadLifecycleManagedRunnable aManagedObject)
  {
    if (aManagedObject == null)
      throw new IllegalArgumentException("must pass non null object");
    synchronized (this)
    {
      if (getState() == IThreadLifecycleManager.STOPPED)
        mManagedObject = aManagedObject;
      else
        throw new RuntimeException("object not currently stopped so can't set a new object");
    }
  }

  @Override
  public ThreadState getState()
  {
    return (ThreadState) super.getState();
  }

  @Override
  public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
  {
    boolean result = false;
    if (event.getSource() == this)
    {
      if (event instanceof InternalEvent)
        // self handling events to make the code a little
        // easier to edit
        result = ((InternalEvent)event).handleEvent(dispatcher, event);
    }

    return result;
  }

  public void addEventHandler(int priority,
      Class<? extends IEvent> eventClass, IEventHandler handler)
  {
    this.getEventDispatcher().addEventHandler(priority, eventClass, handler);
  }

  public void removeEventHandler(int priority,
      Class<? extends IEvent> eventClass, IEventHandler handler)
      throws IndexOutOfBoundsException
  {
    this.getEventDispatcher().removeEventHandler(priority, eventClass, handler);
  }

  public void start()
  {
    this.getEventDispatcher().dispatchEvent(new StartEvent(this));
  }

  public void stop()
  {
    this.getEventDispatcher().dispatchEvent(new StopEvent(this));
  }

  /**
   * The following methods are for calling from the states.
   */
  void setState(ThreadState newState, Throwable t)
  {
    synchronized(this)
    {
      super.setState(newState);
      if (newState == STARTING)
        ++mNumStarting;
      else if (newState == STARTED)
        ++mNumStarted;
      else if (newState == STOPPING)
        ++mNumStopping;
      else if (newState == STOPPED)
        ++mNumStopped;
      // and let anyone waiting on another thread know something
      // has happened
      mWorkerException = t;
      this.notifyAll();
    }
    if (newState == STARTED)
      this.getEventDispatcher().dispatchEvent(new IThreadLifecycleManager.RunnableStarted(this));
    else if (newState == STOPPED)
      this.getEventDispatcher().dispatchEvent(new IThreadLifecycleManager.RunnableStopped(this, t));
  }
  
  void startWorker()
  {
    log.debug("starting worker");
    ThreadWorker worker = new ThreadWorker(this, mManagedObject);
    mExecutor.execute(worker);
  }
  
  protected void finalize()
  {
    if (mInternalExecutor != null) {
      mInternalExecutor.shutdownNow();
      mInternalExecutor = null;
    }
    mExecutor = null;
    mManagedObject = null;
  }
  public void startAndWait(long aWaitTimeout)
  {
    try
    {
      synchronized(this)
      {
        ThreadState state = this.getState();
        
        // don't start unless stopped
        if (state == STOPPED)
        {
          long stoppingCycles = mNumStopping;
          long stoppedCycles = mNumStopped;
          
          this.start();

          while (mNumStopping == stoppingCycles
              && mNumStopped == stoppedCycles
              && (state = this.getState()) != STARTED)
          {
            if (aWaitTimeout == 0)
              this.wait();
            else
            {
              long beforeWait = System.currentTimeMillis();
              this.wait(aWaitTimeout);
              long afterWait = System.currentTimeMillis();
              long delta = afterWait - beforeWait;
              aWaitTimeout -= delta;
              if (aWaitTimeout <= 0)
                // we've actually timed out
                break;
            }
          }
        }
        if (mWorkerException != null)
        {
          RuntimeException ex = null;
          if (mWorkerException instanceof RuntimeException)
            ex = (RuntimeException)mWorkerException;
          else
            ex = new RuntimeException("worker had uncaught exception", mWorkerException);
          throw ex;
        }
      }
    }
    catch (InterruptedException e)
    {
      // if interrupted just gracefully return
      return;
    }
  }
  
  public void stopAndWait(long aWaitTimeout)
  {
    try
    {
      synchronized(this)
      {
        long stoppedCycles = mNumStopped;
        this.stop();
        while (mNumStopped == stoppedCycles
            && this.getState() != STOPPED)
        {
          if (aWaitTimeout == 0)
            this.wait();
          else
          {
            long beforeWait = System.currentTimeMillis();
            this.wait(aWaitTimeout);
            long afterWait = System.currentTimeMillis();
            long delta = afterWait - beforeWait;
            aWaitTimeout -= delta;
            if (aWaitTimeout <= 0)
              // we've actually timed out
              break;
          }
        }
        if (mWorkerException != null)
        {
          RuntimeException ex = null;
          if (mWorkerException instanceof RuntimeException)
            ex = (RuntimeException)mWorkerException;
          else
            ex = new RuntimeException("worker had uncaught exception", mWorkerException);
          throw ex;
        }
      }
    }
    catch (InterruptedException e)
    {
      // if interrupted just gracefully return
      return;
    }
  }
  /**
   * Returns th eobject we're managing.
   * @return The object we're managing.
   */
  public IThreadLifecycleManagedRunnable getManagedObject()
  {
    return mManagedObject;
  }
  
  /**
   * Returns any exception the worker didn't handle before terminating.
   * @return Any unhandled worker exception
   */
  public Throwable getWorkerException()
  {
    return mWorkerException;
  }
  
}
