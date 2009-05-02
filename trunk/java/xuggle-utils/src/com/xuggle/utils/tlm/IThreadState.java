package com.xuggle.utils.tlm;

import com.xuggle.utils.sm.IState;

/**
 * A Thread-Specific state.  All implementations of states
 * must implement this.
 * 
 * @author aclarke
 *
 */
public interface IThreadState extends IState
{
  public abstract void start(ThreadLifecycleManager mgr);
  public abstract void stop(ThreadLifecycleManager mgr);
  public abstract void onStarted(ThreadLifecycleManager mgr);
  public abstract void onStopped(ThreadLifecycleManager mgr, Throwable t);

}
