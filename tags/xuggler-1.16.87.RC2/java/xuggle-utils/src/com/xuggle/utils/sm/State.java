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
package com.xuggle.utils.sm;

/** This is the recommended base class for state machine states.  It
 * provides a name for each state, to make printing neat and easy.
 */

public class State implements IState
{
    /** The state machine. */
  
    protected StateMachine sm;
    
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

//     @Override
//     public void registerStateMachine(StateMachine sm)
//     {
//       this.sm = sm;
//     }

//     /** {@inheritDoc} */

//     @Override
//     public StateMachine getSm()
//     {
//       return sm;
//     }

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
