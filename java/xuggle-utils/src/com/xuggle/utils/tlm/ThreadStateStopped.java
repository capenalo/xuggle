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
 * Internal Only.  The {@link IThreadLifecycleManager#STOPPED} state.
 */
public class ThreadStateStopped extends ThreadState
{
  Logger log = LoggerFactory.getLogger(this.getClass());

  public ThreadStateStopped()
  {
    super("STOPPED");
  }

  @Override
  public void start(ThreadLifecycleManager sm)
  {
    log.debug("starting");
    // and await notification that we've started
    sm.setState(ThreadLifecycleManager.STARTING, null);
    if (sm.getManagedObject() != null)
    {
      sm.startWorker();
    } else {
      log.debug("no object being managed");
      sm.setState(ThreadLifecycleManager.STOPPED, new IllegalStateException("no object being managed"));
    }
  }

  @Override
  public void stop(ThreadLifecycleManager sm)
  {
    log.debug("already stopped; ignoring");
  }

  @Override
  public void onStarted(ThreadLifecycleManager sm)
  {
    log.debug("got unexpected onStarted notification; ignoring");
  }

  @Override
  public void onStopped(ThreadLifecycleManager sm, Throwable t)
  {
    log.debug("got unexpected onStopped notification; ignoring");
  }

}
