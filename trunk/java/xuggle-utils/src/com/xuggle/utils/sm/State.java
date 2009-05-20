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

package com.xuggle.utils.sm;

/** This is the recommended base class for state machine states.  It
 * provides a name for each state, to make printing neat and easy.
 */

public class State implements IState
{
    /** string name of the state, not intended to be a unique identifer
     * for the state */
    
    private String name;

    /** Construct a state. 
     *
     * @param name the name of the state for printing
     */

    public State(String name)
    {
      this.name = name;
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
      return name;
    }
}
