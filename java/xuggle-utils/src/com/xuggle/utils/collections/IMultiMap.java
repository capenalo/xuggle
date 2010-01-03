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
package com.xuggle.utils.collections;

import java.util.Set;

/**
 * Maps N items of type A to M items of class B.
 * 
 * @author aclarke
 *
 * @param <A> The type of the first set of items to map.
 * @param <B> The type of the second set of items to map.
 */
public interface IMultiMap<A, B>
{
  /**
   * Establishes a mapping between a and b.
   * @param a first item to map
   * @param b item to map to
   * @return true if this is a new mapping; false if this mapping already exists.
   */
  boolean unmap(A a, B b);

  /**
   * Removes a mapping between a and b.
   * @param a first item to unmap.
   * @param b item to unmap from.
   * @return true if an existing mapping was found and removed; false if no such
   *   mapping existed.
   */
  boolean map(A a, B b);

  /**
   * Returns all items that are mapped to the given key.
   * @param key The item to find the mappings for.
   * @return An unmodifiable {@link Set} of all objects mapped to key.
   *   Users may safely iterate over this and use returned values as
   *   inputs to {@link #unmap(Object, Object)}.
   */
  Set<B> getMappedB(A key);

  /**
   * Returns all items that are mapped to the given key.
   * @param key The item to find the mappings for.
   * @return An unmodifiable {@link Set} of all objects mapped to key.
   *   Users may safely iterate over this and use returned values as
   *   inputs to {@link #unmap(Object, Object)}.
   */
  Set<A> getMappedA(B key);

  /**
   * Removes all mappings from key to another class.
   * @param key key to remove.
   * @return true if at least one mapping was removed; false otherwise.
   */
  boolean removeAllA(A key);
  /**
   * Removes all mappings from key to another class.
   * @param key key to remove.
   * @return true if at least one mapping was removed; false otherwise.
   */
  boolean removeAllB(B key);
}
