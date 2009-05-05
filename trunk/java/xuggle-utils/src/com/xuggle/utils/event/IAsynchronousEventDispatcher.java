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
