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
package com.xuggle.utils.queue;

import static org.junit.Assert.*;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Queue;

import org.junit.Test;

public class ArrayQueueTest
{

  @Test
  public final void testSize()
  {
    Queue<Integer> q = new ArrayQueue<Integer>();
    assertEquals(0, q.size());
    q.offer(1);
    assertEquals(1, q.size());
    q.offer(0);
    assertEquals(2, q.size());
    q.clear();
    assertEquals(0, q.size());
  }

  @Test
  public final void testArrayQueue()
  {
    new ArrayQueue<Integer>();
    new ArrayQueue<Integer>(1);
    try {
      new ArrayQueue<Integer>(0);
      fail("should not get here");
    } catch (IllegalArgumentException e) {}
  }

  @Test
  public final void testIterator()
  {
    Queue<Integer> q = new ArrayQueue<Integer>(1);
    assertEquals(0, q.size());
    for(int i = 0; i < 8; i++)
      q.offer(i);
    assertEquals(8, q.size());
    Iterator<Integer> iter = q.iterator();
    assertNotNull(iter);
    for(int i = 0; i < 8; i++)
    {
      assertTrue(iter.hasNext());
      assertEquals(i, (int)iter.next());
    }
    assertFalse(iter.hasNext());
    assertNull(iter.next());
    
    iter = q.iterator();
    assertTrue(iter.hasNext());
    assertEquals(0, (int)iter.next());
    // now modify the queue
    assertEquals(0, (int)q.poll());
    try {
      iter.hasNext();
      fail("should not get here");
    } catch (ConcurrentModificationException e) {}
    try {
      iter.next();
      fail("should not get here");
    } catch (ConcurrentModificationException e) {}
  }

  @Test
  public final void testOffer()
  {
    Queue<Integer> q = new ArrayQueue<Integer>();
    assertEquals(0, q.size());
    q.offer(1);
    assertEquals(1, q.size());
    assertEquals(1, (int)q.poll());
    assertEquals(0, q.size());
  }

  @Test
  public final void testPeek()
  {
    Queue<Integer> q = new ArrayQueue<Integer>();
    assertEquals(0, q.size());
    q.offer(1);
    assertEquals(1, q.size());
    assertEquals(1, (int)q.peek());
    assertEquals(1, q.size());
    assertEquals(1, (int)q.poll());
    assertEquals(0, q.size());
  }

  @Test
  public final void testPoll()
  {
    Queue<Integer> q = new ArrayQueue<Integer>(1);
    assertEquals(0, q.size());
    for(int i = 0; i < 8; i++)
      q.offer(i);
    assertEquals(8, q.size());
    for(int i = 0; i < 8; i++)
      assertEquals(i, (int)q.poll());
    assertNull(q.poll());
    assertEquals(0, q.size());
  }

}
