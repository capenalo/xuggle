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

/**
 * Objects that want to be managed by the {@link IThreadLifecycleManager} should
 * implement this interface.
 */
public interface IThreadLifecycleManagedRunnable
{
  /**
   * Called by Worker thread while in {@link IThreadLifecycleManager#STARTING}
   * state to initialize the worker.  Once this returns
   * the manager will transition the object to
   * {@link IThreadLifecycleManager#STARTED}
   * @param mgr The object managing this exception
   * @throws InterruptedException if the thread is asked to stop by the JVM
   */
  public void initialize(IThreadLifecycleManager mgr) throws InterruptedException;

  /**
   * Called by Worker thread while in {@link IThreadLifecycleManager#STARTED}
   * state to initialize the worker.  Once this returns
   * the manager will transition the object to
   * {@link IThreadLifecycleManager#STOPPING}
   * 
   * Well-behaved objects will frequently check that the state
   * of the manager is {@link IThreadLifecycleManager#STARTED} and if not, will exit.
   * @param mgr The object managing this exception
   * @throws InterruptedException if the thread is asked to stop by the JVM
   */
  public void execute(IThreadLifecycleManager mgr) throws InterruptedException;
  
  /**
   * Called by Worker thread while in {@link IThreadLifecycleManager#STOPPING}
   * state to finalize the worker.  Once this returns
   * the manager will transition the object to
   * {@link IThreadLifecycleManager#STOPPED}
   * @param mgr The object managing this exception
   * @param ex If the object generated an uncaught exception, it is passed in here; else null
   * @return true if the manager should catch this error; false if the manager should let it propagate.
   *   The return value is ignored if no exception is passed to this method.
   * @throws InterruptedException if the thread is asked to stop by the JVM
   */
  public boolean finish(IThreadLifecycleManager mgr, Throwable ex) throws InterruptedException;
}
