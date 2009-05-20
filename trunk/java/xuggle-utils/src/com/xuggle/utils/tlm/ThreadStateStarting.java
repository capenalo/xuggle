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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal Only.  The {@link IThreadLifecycleManager#STARTING} state.
 */
class ThreadStateStarting extends ThreadState
{
  Logger log = LoggerFactory.getLogger(this.getClass());

  public ThreadStateStarting()
  {
    super("STARTING");
  }

  @Override
  public void start(ThreadLifecycleManager sm)
  {
    log.debug("got start() while starting; ignoring");
  }

  @Override
  public void stop(ThreadLifecycleManager sm)
  {
    log.debug("got stop() while starting; heading back to STOPPED");
    sm.setState(ThreadLifecycleManager.STOPPED, null);
  }

  @Override
  public void onStarted(ThreadLifecycleManager sm)
  {
    log.debug("got onStarted(); going to STARTED");
    sm.setState(ThreadLifecycleManager.STARTED, null);
  }

  @Override
  public void onStopped(ThreadLifecycleManager sm, Throwable t)
  {
    log.debug("got onStopped() while starting; an error occurred and we should stop");
    sm.setState(ThreadLifecycleManager.STOPPED, t);
  }

}
