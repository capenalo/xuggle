package com.xuggle.utils.event.handler;

import com.xuggle.utils.event.IEvent;
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.IEventHandler;

/**
 * An event handler wrapper that proxies another {@link IEventHandler} but
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
   *  the proxiedHandler will only be
   *  called if {@link IEvent#getSource()}.equals(source).
   *  @param proxiedHandler The handler we're proxying for.
   *  
   *  @throws IllegalArgumentException if proxiedHandler == null
   */
  protected TargetedHandler(Object source, IEventHandler<E> proxiedHandler)
  {
    mSource = source;
    if (proxiedHandler == null)
      throw new IllegalArgumentException();
    mHandler = proxiedHandler;
  }
  
  /**
   * The {@link IEvent#getSource()} of an object must
   * equal this return value in order for us to fire
   * our {@link #getProxiedHandler()} handler.
   * @return The source we're targeted on.
   */
  public Object getTargetedSource()
  {
    return mSource;
  }
  
  /**
   * Returns the {@link IEventHandler} this object calls
   * when its {@link #handleEvent(IEventDispatcher, IEvent)} method
   * is called.
   * @return the proxied event handler.
   */
  public IEventHandler<E> getProxiedHandler()
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
    
    return mHandler.handleEvent(dispatcher, event);
  }
}
