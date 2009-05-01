package com.xuggle.utils.event.handler;

import com.xuggle.utils.event.IEvent;
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.IEventHandler;

/**
 * An event handler wrapper that wraps another {@link IEventHandler} but
 * only calls it if 
 *  {@link IEvent#getSource()} is the same as the source
 *  this object is created with.
 * @author aclarke
 *
 * @param <E> The class of event being handled
 */
public class TargetedHandler<E extends IEvent> implements IEventHandler<E>
{
  private final Object mSource;
  private final IEventHandler<E> mHandler;
  
  /**
   * Creates a new object.
   * @param source if non-null, then
   *  {@link #implHandleEvent(IEventDispatcher, IEvent)} will only be
   *  called if {@link IEvent#getSource()}.equals(source).
   */
  protected TargetedHandler(Object source, IEventHandler<E> handler)
  {
    mSource = source;
    if (handler == null)
      throw new IllegalArgumentException();
    mHandler = handler;
  }
  
  public Object getTargetedSource()
  {
    return mSource;
  }
  
  public IEventHandler<E> getTargetedHandler()
  {
    return mHandler;
  }
  
  /**
   * The handleEvent that {@link IEventDispatcher} will use.  Do not
   * override
   */
  public boolean handleEvent(IEventDispatcher dispatcher, E event)
  {
    if (mSource != null && !mSource.equals(event.getSource()))
      return false;
    if (this == mHandler)
      // we will not forward to ourselves
      return false;
    
    return mHandler.handleEvent(dispatcher, event);
  }
}
