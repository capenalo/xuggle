/*
 * This file is part of Xuggler.
 * 
 * Xuggler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Xuggler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Xuggler.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xuggle.utils.tlm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal Only.  The {@link IThreadLifecycleManager#STOPPED} state.
 */
class ThreadStateStopped extends ThreadState
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
