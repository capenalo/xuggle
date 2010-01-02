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
  TargetedHandler(Object source, IEventHandler<E> proxiedHandler)
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
