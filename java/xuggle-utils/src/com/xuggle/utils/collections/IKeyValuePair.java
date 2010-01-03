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

/**
 * A key-value pair.
 * 
 * @author aclarke
 * 
 */
public interface IKeyValuePair
{
  /**
   * Get the key.
   * 
   * @return the key.
   */
  public String getKey();

  /**
   * Get the value.
   * 
   * @return the value.
   */
  public String getValue();
}