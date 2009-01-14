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

import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import com.xuggle.test_utils.NameAwareTestClassRunner;
import com.xuggle.utils.event.AsynchronousEventDispatcher;
import com.xuggle.utils.tlm.IThreadLifecycleManagedRunnable;
import com.xuggle.utils.tlm.IThreadLifecycleManager;
import com.xuggle.utils.tlm.ThreadLifecycleManager;

import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(NameAwareTestClassRunner.class)
public class ThreadLifecycleManagerTest
{
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private String mTestName = null;
  private IThreadLifecycleManagedRunnable mDummyObj = null; 

  @Before
  public void setUp()
  {
    mTestName = NameAwareTestClassRunner.getTestMethodName();
    log.debug("-----START-----: {}", mTestName);
    mDummyObj = new IThreadLifecycleManagedRunnable(){
      private final Logger log = LoggerFactory.getLogger(this.getClass());

      
      public void execute(IThreadLifecycleManager aMgr)
      {
        log.debug("execute");
        return;
      }

      
      public boolean finish(IThreadLifecycleManager aMgr, Throwable ex)
      {
        log.debug("finish");
        return true;
      }

      
      public void initialize(IThreadLifecycleManager aMgr)
      {
        log.debug("initialize");
      }
      
    };

  }

  @After
  public void tearDown()
  {
    log.debug("----- END -----: {}", mTestName);
  }

  /**
   * This test just ensures that their is at least one valid (passing)
   * test in this file.
   */
  @Test
  public void testSuccess()
  {
    assertTrue(true);
  }

  /**
   * Tests that we can create and destroy a tlm
   * @throws InterruptedException because, well, sometimes it can (but that's an error, see)
   */
  @Test(timeout=1000)
  public void testCreationSuccess() throws InterruptedException
  {
    IThreadLifecycleManager mgr = null;
    ExecutorService executor = null;
    
    mgr = new ThreadLifecycleManager(mDummyObj);
    assertNotNull(mgr);
    
    mgr = new ThreadLifecycleManager(mDummyObj, new AsynchronousEventDispatcher());
    assertNotNull(mgr);
    
    executor = Executors.newFixedThreadPool(1);
    assertNotNull(executor);
    mgr = new ThreadLifecycleManager(mDummyObj, new AsynchronousEventDispatcher(),
        executor);
    assertNotNull(mgr);
    executor.shutdown();
    executor.awaitTermination(100, TimeUnit.MILLISECONDS);
  }
  
  /**
   * Tests that we fail to create tlms under certain circumstances
   */
  @Test(timeout=1000, expected=IllegalArgumentException.class)
  public void testCreationFailure()
  {
    ThreadLifecycleManager mgr = new ThreadLifecycleManager(null);
    assertNotNull(mgr);
    // should throw exception
    mgr.setManagedObject(null);
  }
  
  /**
   * Tests that we can start a TLM, start it, and then
   * stop it.
   * @throws InterruptedException Well, really it should fail it we do...
   */
  @Test(timeout=1000)
  public void testSimpleStartStop() throws InterruptedException
  {
    IThreadLifecycleManager mgr = null;
    ExecutorService executor = Executors.newFixedThreadPool(1);
    AsynchronousEventDispatcher dispatcher = new AsynchronousEventDispatcher(true);
    
    try
    {
      log.debug("creating tlm");
      mgr = new ThreadLifecycleManager(
          new IThreadLifecycleManagedRunnable(){
            private final Logger log = LoggerFactory.getLogger(this.getClass());

            
            public void execute(IThreadLifecycleManager aMgr) throws InterruptedException
            {
              log.debug("awaiting stopping request");
              while(aMgr.getState() == IThreadLifecycleManager.STARTED)
                Thread.sleep(100);
              log.debug("stopping");
            }

            
            public boolean finish(IThreadLifecycleManager aMgr, Throwable ex)
            {
              log.debug("finish");
              return true;
            }

            
            public void initialize(IThreadLifecycleManager aMgr)
            {
              log.debug("initialize");
            }
            
          },
          dispatcher, executor);
      // start up the main thread
      log.debug("starting tlm");
      mgr.start();
      log.debug("waiting for tlm to finish starting");
      // wait for it to start-up
      while(mgr.getState() != IThreadLifecycleManager.STARTED)
        Thread.sleep(10);
      // then tell the executor to shut down once the manager stops
      log.debug("shutting down executor");
      executor.shutdown();

      // and tell it to stop
      log.debug("telling mgr to stop");
      mgr.stop();
      log.debug("waiting for mgr to stop");
      while(mgr.getState() != IThreadLifecycleManager.STOPPED)
        Thread.sleep(10);
      log.debug("now stopped");

    }
    finally
    {
      log.debug("waiting for executor to finish");
      executor.awaitTermination(100, TimeUnit.MILLISECONDS);
      log.debug("stopping dispatcher");
      dispatcher.stopDispatching();
      log.debug("waiting for dispatcher to finish");
      dispatcher.waitForDispatcherToFinish(0);
      log.debug("all done");
    }
  }

  /**
   * Tests that we can start a TLM, start it, and then
   * stop it, but that we can have the TLM do the heavly
   * lifting waiting for us
   * 
   * @throws InterruptedException Well, really it should fail it we do...
   */
  @Test(timeout=1000)
  public void testSimplerStartStop() throws InterruptedException
  {
    IThreadLifecycleManager mgr = null;
    ExecutorService executor = Executors.newFixedThreadPool(1);
    AsynchronousEventDispatcher dispatcher = new AsynchronousEventDispatcher(true);
    
    try
    {
      log.debug("creating tlm");
      mgr = new ThreadLifecycleManager(
          new IThreadLifecycleManagedRunnable(){
            private final Logger log = LoggerFactory.getLogger(this.getClass());

            
            public void execute(IThreadLifecycleManager aMgr) throws InterruptedException
            {
              log.debug("awaiting stopping request");
              synchronized(aMgr)
              {
                while(aMgr.getState() == IThreadLifecycleManager.STARTED)
                  aMgr.wait();
              }
              log.debug("stopping");
            }

            
            public boolean finish(IThreadLifecycleManager aMgr, Throwable ex)
            {
              log.debug("finish");
              return true;
            }

            
            public void initialize(IThreadLifecycleManager aMgr)
            {
              log.debug("initialize");
            }
            
          },
          dispatcher, executor);
      // start up the main thread
      log.debug("starting tlm");
      mgr.startAndWait(0);
      assertEquals(IThreadLifecycleManager.STARTED, mgr.getState());
      // then tell the executor to shut down once the manager stops
      log.debug("shutting down executor");
      executor.shutdown();

      // and tell it to stop
      log.debug("telling mgr to stop");
      mgr.stopAndWait(0);
      log.debug("waiting for mgr to stop");
      assertEquals(IThreadLifecycleManager.STOPPED, mgr.getState());
      log.debug("now stopped");

    }
    finally
    {
      log.debug("waiting for executor to finish");
      executor.awaitTermination(100, TimeUnit.MILLISECONDS);
      log.debug("stopping dispatcher");
      dispatcher.stopDispatching();
      log.debug("waiting for dispatcher to finish");
      dispatcher.waitForDispatcherToFinish(0);
      log.debug("all done");
    }
  }

  /**
   * Tests {@link ThreadLifecycleManager#startAndWait(long)} by
   * creating a managed object that never starts correctly, and
   * ensuring the start returns after a period without correctly
   * transitioning states.
   *  
   * @throws InterruptedException Well, really it should fail it we do...
   */
  @Test(timeout=1000)
  public void testStartAndWait() throws InterruptedException
  {
    IThreadLifecycleManager mgr = null;
    ExecutorService executor = Executors.newFixedThreadPool(1);
    AsynchronousEventDispatcher dispatcher = new AsynchronousEventDispatcher(true);
    final RuntimeException workerException = new RuntimeException();
    
    try
    {
      log.debug("creating tlm");
      mgr = new ThreadLifecycleManager(
          new IThreadLifecycleManagedRunnable(){
            private final Logger log = LoggerFactory.getLogger(this.getClass());

            
            public void execute(IThreadLifecycleManager aMgr) throws InterruptedException
            {
              synchronized(aMgr)
              {
                while(aMgr.getState() == IThreadLifecycleManager.STARTED)
                  aMgr.wait();
              }
            }

            
            public boolean finish(IThreadLifecycleManager aMgr, Throwable ex) throws InterruptedException
            {
              log.debug("finish");
              // for this test, the initialize should throw an exception
              workerException.initCause(ex);
              return true;
            }

            
            public void initialize(IThreadLifecycleManager aMgr) throws InterruptedException
            {
              log.debug("initialize");
              // and wait at least 200 seconds
              Thread.sleep(200);
            }
            
          },
          dispatcher, executor);
      // start up the main thread
      log.debug("starting tlm");
      long startTime = System.currentTimeMillis();
      mgr.startAndWait(100);
      long finishTime = System.currentTimeMillis();
      long delta = finishTime - startTime;
      assertEquals(IThreadLifecycleManager.STARTING, mgr.getState());
      assertTrue("didn't wait long enough: " + delta, delta >= 100);
      mgr.stopAndWait(0);
      // make sure we actually got an exception in finish
      assertNull("found unexpected cause for exception", workerException.getCause());
      // then tell the executor to shut down once the manager stops
      log.debug("shutting down executor");
      executor.shutdown();
      
    }
    finally
    {
      log.debug("waiting for executor to finish");
      executor.awaitTermination(100, TimeUnit.MILLISECONDS);
      log.debug("stopping dispatcher");
      dispatcher.stopDispatching();
      log.debug("waiting for dispatcher to finish");
      dispatcher.waitForDispatcherToFinish(0);
      log.debug("all done");
    }
  }

  @Test(timeout=1000)
  public void testFinishMethodGetsException() throws InterruptedException
  {
    IThreadLifecycleManager mgr = null;
    ExecutorService executor = Executors.newFixedThreadPool(1);
    AsynchronousEventDispatcher dispatcher = new AsynchronousEventDispatcher(true);
    final RuntimeException workerException = new RuntimeException();
    
    try
    {
      log.debug("creating tlm");
      mgr = new ThreadLifecycleManager(
          new IThreadLifecycleManagedRunnable(){
            private final Logger log = LoggerFactory.getLogger(this.getClass());

            
            public void execute(IThreadLifecycleManager aMgr) throws InterruptedException
            {
              fail("should never get to calling this for this test");
            }

            
            public boolean finish(IThreadLifecycleManager aMgr, Throwable ex)
            {
              log.debug("finish");
              // for this test, the initialize should throw an exception
              workerException.initCause(ex);
              return true;
            }

            
            public void initialize(IThreadLifecycleManager aMgr)
            {
              log.debug("initialize");
              throw new RuntimeException("test to make sure initialization doesn't work");
            }
            
          },
          dispatcher, executor);
      // start up the main thread
      log.debug("starting tlm");
      mgr.startAndWait(0);
      assertEquals(IThreadLifecycleManager.STOPPED, mgr.getState());
   
      // make sure we actually got an exception in finish
      assertNotNull("did not find expected cause for exception", workerException.getCause());
      assertTrue("cause not of expected type", workerException.getCause() instanceof RuntimeException);
      // then tell the executor to shut down once the manager stops
      log.debug("shutting down executor");
      executor.shutdown();
      
    }
    finally
    {
      log.debug("waiting for executor to finish");
      executor.awaitTermination(100, TimeUnit.MILLISECONDS);
      log.debug("stopping dispatcher");
      dispatcher.stopDispatching();
      log.debug("waiting for dispatcher to finish");
      dispatcher.waitForDispatcherToFinish(0);
      log.debug("all done");
    }
  }

  /**
   * Tests {@link ThreadLifecycleManager#startAndWait(long)} by
   * creating a managed object that never starts correctly, and
   * ensuring the start returns after a period without correctly
   * transitioning states.
   *  
   * @throws InterruptedException Well, really it should fail it we do...
   */
  @Test(timeout=1000)
  public void testStopAndWait() throws InterruptedException
  {
    final long testDuration = 300;
    IThreadLifecycleManager mgr = null;
    ExecutorService executor = Executors.newFixedThreadPool(1);
    AsynchronousEventDispatcher dispatcher = new AsynchronousEventDispatcher(true);
    final RuntimeException workerException = new RuntimeException();
    
    try
    {
      log.debug("creating tlm");
      mgr = new ThreadLifecycleManager(
          new IThreadLifecycleManagedRunnable(){
            private final Logger log = LoggerFactory.getLogger(this.getClass());

            
            public void execute(IThreadLifecycleManager aMgr) throws InterruptedException
            {
              log.debug("execute: sleeping for {}", testDuration);
              Thread.sleep(testDuration);
              log.debug("execute: done sleeping;");
            }

            
            public boolean finish(IThreadLifecycleManager aMgr, Throwable ex) throws InterruptedException
            {
              log.debug("finish: sleeping for {}", testDuration);
              // for this test, the initialize should throw an exception
              workerException.initCause(ex);
              // sleep long enough for stopAndWait() to timeout
              Thread.sleep(testDuration);
              log.debug("finish: done sleeping;");
              return true;
            }

            
            public void initialize(IThreadLifecycleManager aMgr)
            {
              log.debug("initialize");
            }
            
          },
          dispatcher, executor);
      // start up the main thread
      log.debug("starting tlm");
      mgr.startAndWait(0);
      assertEquals(IThreadLifecycleManager.STARTED, mgr.getState());

      long startTime = System.currentTimeMillis();
      // this should timeout because the worker is locked in a sleep
      // of longer duration
      mgr.stopAndWait(100);
      long finishTime = System.currentTimeMillis();
      long delta = finishTime - startTime;
      assertEquals(IThreadLifecycleManager.STOPPING, mgr.getState());
      assertTrue("didn't wait long enough: " + delta, delta >= 100);
      synchronized(mgr)
      {
        // now wait for it to actually finish
        while(mgr.getState() != IThreadLifecycleManager.STOPPED)
          mgr.wait();
      }
      
      // then tell the executor to shut down once the manager stops
      log.debug("shutting down executor");
      executor.shutdown();
      
    }
    finally
    {
      log.debug("waiting for executor to finish");
      executor.awaitTermination(100, TimeUnit.MILLISECONDS);
      log.debug("stopping dispatcher");
      dispatcher.stopDispatching();
      log.debug("waiting for dispatcher to finish");
      dispatcher.waitForDispatcherToFinish(0);
      log.debug("all done");
    }
  }
  
  @Test(timeout=1000)
  public void testWorkerExitsBeforeWeWaitForStarting()
  {
    IThreadLifecycleManagedRunnable worker = new IThreadLifecycleManagedRunnable()
    {
      
      public void execute(IThreadLifecycleManager aMgr)
          throws InterruptedException
      {
      }
      
      public boolean finish(IThreadLifecycleManager aMgr, Throwable aEx)
          throws InterruptedException
      {
        return false;
      }
      
      public void initialize(IThreadLifecycleManager aMgr)
          throws InterruptedException
      {
      }
    };
    IThreadLifecycleManager mgr = new ThreadLifecycleManager(worker);
    mgr.startAndWait(0);
    // depending on thread timing, this could be STARTED for an instance,
    // but rest assured it's dying after this
    mgr.stopAndWait(0);
    assertEquals(IThreadLifecycleManager.STOPPED, mgr.getState());

  }

  @Test(timeout=1000)
  public void testSetManagedObject()
  {
    ThreadLifecycleManager mgr = new ThreadLifecycleManager();
    assertNull(mgr.getManagedObject());
    mgr.setManagedObject(mDummyObj);
    assertNotNull(mgr.getManagedObject());
  }
  @Test(timeout=1000, expected=RuntimeException.class)
  public void testSetManagedObjectFailsWhileRunning()
  {
    IThreadLifecycleManagedRunnable worker = new IThreadLifecycleManagedRunnable()
    {
      
      public void execute(IThreadLifecycleManager aMgr)
          throws InterruptedException
      {
        synchronized(aMgr)
        {
          while (aMgr.getState() == IThreadLifecycleManager.STARTED)
            aMgr.wait();
        }
      }
      
      public boolean finish(IThreadLifecycleManager aMgr, Throwable aEx)
          throws InterruptedException
      {
        return false;
      }
      
      public void initialize(IThreadLifecycleManager aMgr)
          throws InterruptedException
      {
      }
    };
    ThreadLifecycleManager mgr = new ThreadLifecycleManager(worker);
    try
    {
      mgr.startAndWait(0);
      assertEquals(IThreadLifecycleManager.STARTED, mgr.getState());
      // set a new object while running; should fail
      mgr.setManagedObject(mDummyObj);
    }
    finally
    {
      mgr.stopAndWait(0);
      assertEquals(IThreadLifecycleManager.STOPPED, mgr.getState());
    }
  }
  @Test(timeout=1000)
  public void testStartFailsWithNoManagedObject() throws InterruptedException
  {
    ThreadLifecycleManager mgr = new ThreadLifecycleManager(); // no worker
    // attempt to start without the worker
    synchronized(mgr)
    {
      mgr.start();
      Throwable t = mgr.getWorkerException();
      while (t == null)
      {
        mgr.wait();
        t = mgr.getWorkerException();
      }
      assertNotNull(t);
      assertEquals(IThreadLifecycleManager.STOPPED, mgr.getState());
    }
  }
  
  @Test(timeout=1000, expected=IllegalStateException.class)
  public void testStartAndWaitRaisesExceptionIfWorkerInitializeRaisesException()
  {
    IThreadLifecycleManagedRunnable worker = new IThreadLifecycleManagedRunnable()
    {

      
      public void initialize(IThreadLifecycleManager aMgr)
          throws InterruptedException
      {
        throw new IllegalStateException("couldn't start");
      }
      
      
      public void execute(IThreadLifecycleManager aMgr)
          throws InterruptedException
      {
      }

      
      public boolean finish(IThreadLifecycleManager aMgr, Throwable aEx)
          throws InterruptedException
      {
        return false;
      }

    };
    IThreadLifecycleManager mgr = new ThreadLifecycleManager(worker);
    mgr.startAndWait(0);
  }

}
