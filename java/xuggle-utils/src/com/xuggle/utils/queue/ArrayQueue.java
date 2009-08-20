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
package com.xuggle.utils.queue;

import java.util.AbstractQueue;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * An implementation of {@link Queue}, based on an expanding array.
 * <p>
 * This implementation will be faster than a {@link LinkedList} 
 * but at the expense of being less memory efficient.  It also does
 * not support removal via iterators.
 * </p>
 * <p>
 * It is implemented with a constantly growing backing array (it will
 * never shrink), so is best used for queues that effectively have
 * a maximum size.  It also uses a 'circular buffer' approach to the array
 * so that {@link #offer(Object)} and {@link #poll()} operations
 * are just integer adds (except for the case where the array needs to grow).
 * </p>
 * @author aclarke
 *
 * @param <E> the type of elements to be contained in the queue
 */
public class ArrayQueue<E> extends AbstractQueue<E> implements Queue<E>
{
  private E[] mQueue;
  private int mSize;
  private int mFront;
  private int mBack;
  
  private static int DEFAULT_SIZE=10;
  public ArrayQueue()
  {
    this(DEFAULT_SIZE);
  }
  @SuppressWarnings("unchecked")
  public ArrayQueue(int startingCapacity)
  {
    if (startingCapacity <= 0)
      throw new IllegalArgumentException();
    mQueue = (E[])new Object[startingCapacity];
    mSize = 0;
    mFront = 0;
    mBack = -1;
  }
  @SuppressWarnings("unchecked")
  private void growQueue()
  {
    E[] newQueue = (E[])new Object[mQueue.length*2];
    for(int i = 0; i < mSize; i++, mFront = increment(mFront))
      newQueue[i] = mQueue[mFront];
    mFront = 0;
    mBack = mSize -1;
    mQueue = newQueue;
  }
  private int increment(final int pos)
  {
    final int incr = pos+1;
    if (incr== mQueue.length)
      return 0;
    return incr;
  }
  private int getOffset(final int index)
  {
    if (index >= mSize || index < 0)
      return -1;
    final int offset = mFront + index;
    if (offset >= mQueue.length)
      // wraps
      return offset - mQueue.length;
    else
      return offset;
  }

  private static class ArrayQueueIterator<E> implements Iterator<E>
  {
    private final ArrayQueue<E> mQueue;
    private final int mQFront;
    private final int mQBack;
    private final E[] mQQueue;
    private final int mQSize;
    
    private int mIter;
    
    public ArrayQueueIterator(ArrayQueue<E> q)
    {
      mQueue = q;
      mQFront = q.mFront;
      mQBack = q.mBack;
      mQQueue = q.mQueue;
      mQSize = q.mSize;
      
      mIter = mQFront;
    }
    private boolean wasModified()
    {
      final boolean result;
      result = !(mQFront == mQueue.mFront
          && mQBack == mQueue.mBack
          && mQSize == mQueue.mSize
          && mQQueue == mQueue.mQueue);
      return result;
    }
    public boolean hasNext()
    {
      if (wasModified())
        throw new ConcurrentModificationException();
      if (mIter < mQSize)
        return true;
      return false;
    }

    public E next()
    {
      if (wasModified())
        throw new ConcurrentModificationException();
      final int offset = mQueue.getOffset(mIter);
      if (offset < 0)
        return null;
      ++mIter;
      return mQQueue[offset];
    }

    public void remove()
    {
      throw new UnsupportedOperationException();
    }
    
  }
  public Iterator<E> iterator()
  {
    return new ArrayQueueIterator<E>(this);
  }

  public int size()
  {
    return mSize;
  }

  public boolean offer(E obj)
  {
    if (mSize == mQueue.length)
      growQueue();
    mBack = increment(mBack);
    mQueue[mBack] = obj;
    mSize++;
    return true;
  }

  public E peek()
  {
    return mSize == 0 ? null : mQueue[mFront];
  }

  public E poll()
  {
    if (mSize == 0)
      return null;
    mSize--;
    E retval = mQueue[mFront];
    mQueue[mFront] = null; // clear the reference
    mFront = increment(mFront);
    return retval;
  }

}
