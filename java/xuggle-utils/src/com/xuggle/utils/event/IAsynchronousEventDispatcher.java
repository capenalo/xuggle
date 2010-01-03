/*******************************************************************************
 * Copyright (c) 2008, 2010 Xuggle Inc.  All rights reserved.
 *  
 * This file is part of Xuggle-Utils.
 *
 * Xuggle-Utils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Xuggle-Utils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Xuggle-Utils.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

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
   * tell that thread to stop running.  All events currently
   * in the queue will be dispatched first.
   */
  
  public abstract void stopDispatching();

  /**
   * If running a dispatcher in a separate thread, this will
   * tell that thread to abort.  All event currently in the
   * queue will be discarded without dispatching.
   */
  public void abortDispatching();

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
