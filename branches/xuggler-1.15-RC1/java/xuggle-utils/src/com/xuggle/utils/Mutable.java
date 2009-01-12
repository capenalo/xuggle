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
package com.xuggle.utils;

/**
 * This class just contains an arbitrary value, and allows you to set and
 * reset it.
 * <p/>
 * It is not "thread-safe"; people using this should ensure that access
 * is coordinated.
 * <p/>
 * This can be useful when you want to pass a condition variable
 * to an anonymous routine.
 * <code>
 * <pre>
 * final Mutable&lt;Boolean&gt; b(false);
 * 
 * Thread thread = new Thread(new Runnable(){
 *
 *    public void run()
 *    {
 *      synchronized(b)
 *      {
 *        b.set(true);
 *      }
 *    }
 *    
 *  });
 *  synchronized(b)
 *  {
 *    thread.start();
 *    b.set(false);
 *  }
 *  
 * </pre>
 * </code>
 * @author aclarke
 * @param <E> The type to make mutable.  Will usually be a java primitive since those
 *   are the classes not mutable by default.
 */
public class Mutable<E>
{
  private E value=null;

  /**
   * Make nothing mutable.
   */
  public Mutable()
  {
    
  }
  
  /**
   * Create a wrapper containing a value.
   * @param value The value to contain.
   */
  public Mutable(E value)
  {
    this.value = value;
  }
  /**
   * Set the mutable value.
   * @param value The new mutable value.
   */
  public void set(E value)
  {
    this.value = value;
  }
  /**
   * Get the current value.
   * @return the current value.
   */
  public E get()
  {
    return this.value;
  }
}
