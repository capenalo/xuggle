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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The actual worker object that directly executes the {@link IThreadLifecycleManagedRunnable} object.
 * 
 * This class only has package level visibility as only the {@link ThreadLifecycleManager} and the
 * internal package tests should use it.
 * @author aclarke
 *
 */
class ThreadWorker implements Runnable
{
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  // Manager for object we're managing
  ThreadLifecycleManager mMgr = null;
  // Object who's life cycle we're managing
  IThreadLifecycleManagedRunnable mObj = null;
  
  public ThreadWorker(ThreadLifecycleManager mgr,
      IThreadLifecycleManagedRunnable obj)
  {
    if (mgr == null)
      throw new NullPointerException();
    mMgr = mgr;
    mObj = obj;
  }
  
  public void run()
  {
    log.debug("Starting worker");
    boolean callingFinish = false;
    Throwable uncaughtException = null;
    try
    {
      IThreadState state = null;
      // wait for the state machine to acknowledge we're starting
      synchronized(mMgr)
      {
        while((state = mMgr.getState()) == IThreadLifecycleManager.STOPPED)
          mMgr.wait();
        if (state != IThreadLifecycleManager.STARTING)
          throw new InterruptedException("not starting");
      }      
      // now, call the initialize method
      mObj.initialize(mMgr);
      
      // send the started event
      mMgr.dispatchEvent(mMgr.new StartedEvent(mMgr));
      
      synchronized(mMgr)
      {
        while((state = mMgr.getState()) == IThreadLifecycleManager.STARTING)
          mMgr.wait();
        if (state != IThreadLifecycleManager.STARTED)
          throw new InterruptedException("not started");
      }      
      // run the main loop; slight race here but we take
      // the risk
      mObj.execute(mMgr);
      
      // send the stopping event
      mMgr.dispatchEvent(mMgr.new StopEvent(mMgr));      

      synchronized(mMgr)
      {
        while ((state = mMgr.getState()) == IThreadLifecycleManager.STARTED)
          mMgr.wait();
        if (state != IThreadLifecycleManager.STOPPING)
          throw new InterruptedException("not stopping"); 
      }
      callingFinish = true;
      mObj.finish(mMgr, null);
    }
    catch (Throwable ex)
    {
      // Catch any other error and pass it through to finish
      try
      {
        // always call the finalize method
        if (!callingFinish)
        {
          // don't call finish if it looks like calling it in
          // the first place generated the exception
          if (!mObj.finish(mMgr, ex))
          {
            throw ex;
          }
        }
        else
        {
          throw ex;
        }
      }
      catch (InterruptedException ex2)
      {
        // if interrupted, just return
        return;
      }
      catch (Throwable ex2)
      {
        log.error("uncaught exception: {}", ex2);
        uncaughtException = ex2;
      }
    }
    finally
    {
      // exit to send the stopped event

      // if we're exiting, always send a stopped event.
      mMgr.dispatchEvent(mMgr.new StoppedEvent(mMgr, uncaughtException));
      log.debug("worker finishing");
    }
  }

}
