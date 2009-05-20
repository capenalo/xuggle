/*
 * This file is part of Xuggler.
 * 
 * Xuggler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Xuggler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Xuggler.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xuggle.utils.sm;

/**
 * This is the base Interface for ALL states in The Yard.
 * 
 * @author trebor
 *
 */
public interface IState
{
//     /** Register the state machine with which this state will be operate. 
//      *
//      * @param sm the state machine with which this state will be operate.
//      */

//     public void registerStateMachine(StateMachine sm);

//     /** Get the registered state machine. 
//      *
//      * @return the registered state machine.
//      */

//     public StateMachine getSm();

    /** Get printable version of this state. 
     *
     * @return the string representation of this state.
     */
    
    public String toString();
}
