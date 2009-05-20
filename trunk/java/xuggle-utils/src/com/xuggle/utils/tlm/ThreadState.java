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

import com.xuggle.utils.sm.State;

/**
 * Internal Only. An abstract base class for states a thread can be in.
 */
abstract class ThreadState extends State implements IThreadState
{
  public ThreadState(String name)
  {
    super(name);
  }
  
  public abstract void start(ThreadLifecycleManager mgr);
  public abstract void stop(ThreadLifecycleManager mgr);
  public abstract void onStarted(ThreadLifecycleManager mgr);
  public abstract void onStopped(ThreadLifecycleManager mgr, Throwable t);

}
