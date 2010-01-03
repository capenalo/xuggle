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

package com.xuggle.utils.sm;

/** This is the recommended base class for state machine states.  It
 * provides a name for each state, to make printing neat and easy.
 */

public class State implements IState
{
    /** string name of the state, not intended to be a unique identifer
     * for the state */
    
    private String mName;

    /** Construct a state. 
     *
     * @param name the name of the state for printing
     */

    public State(String name)
    {
      this.mName = name;
    }

    /** {@inheritDoc} */
    
    @Override
    public String toString()
    {
      return getName();
    }

    /** Get the name of this state.
     *
     * @return the name of this state.
     */

    public String getName()
    {
      return mName;
    }
}
