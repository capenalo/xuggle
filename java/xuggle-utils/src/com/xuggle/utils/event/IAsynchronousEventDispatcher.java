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

package com.xuggle.utils.event;

/**
 * An {@link IEventDispatcher} that dispatches events on a separate (asynchronous) thread.
 */
public interface IAsynchronousEventDispatcher extends IEventDispatcher
{

  /**
   * Start a dispatcher running.
   */
  
  public abstract void startDispatching();

  /**
   * If running a dispatcher in a separate thread, this will
   * tell that thread to stop running.
   */
  
  public abstract void stopDispatching();

  /**
   * Waits for the dispatcher thread
   * to finish IF the dispatcher is running on a separate thread.
   * 
   * @param timeout Time, in milliseconds, to wait for the dispatcher
   *   thread to finish.  0 means wait indefinitely.
   */
  
  public abstract void waitForDispatcherToFinish(long timeout);

  /**
   * Is the dispatcher thread currently running?
   * 
   * @return true if the dispatcher is running; false otherwise.
   */
  public abstract boolean isDispatching();

}
