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
 * Internal Only.  The {@link IThreadLifecycleManager#STARTED} state.
 */
class ThreadStateStarted extends ThreadState
{
  Logger log = LoggerFactory.getLogger(this.getClass());

  public ThreadStateStarted()
  {
    super("STARTED");
  }

  @Override
  public void start(ThreadLifecycleManager sm)
  {
    log.debug("got start() but already started; ignoring");
  }

  @Override
  public void stop(ThreadLifecycleManager sm)
  {
    log.debug("got stop(); stopping");
    sm.setState(ThreadLifecycleManager.STOPPING, null);
  }

  @Override
  public void onStarted(ThreadLifecycleManager sm)
  {
    log.debug("got onStarted() but already started; ignoring");
  }

  @Override
  public void onStopped(ThreadLifecycleManager sm, Throwable t)
  {
    log.error("got onStopped() while in Start.  worker has gotten end on all streams");
    sm.setState(ThreadLifecycleManager.STOPPED, t);
  }

}
