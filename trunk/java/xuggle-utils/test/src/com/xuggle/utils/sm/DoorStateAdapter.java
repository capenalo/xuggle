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

import com.xuggle.utils.sm.State;

public class DoorStateAdapter extends State
{
    /** The door state adapter from which door states should be
     * derrived.  
     *
     * @param name the string name of the state.
     */

    public DoorStateAdapter(String name)
    {
      super(name);
    }

    public void open(DoorStateMachine sm)
    {
    }

    public void opened(DoorStateMachine sm)
    {
    }
    
    public void close(DoorStateMachine sm)
    {
    }

    public void closed(DoorStateMachine sm)
    {
    }
    
    public void unlock(DoorStateMachine sm, boolean hasKey)
    {
    }

    public void lock(DoorStateMachine sm, boolean hasKey)
    {
    }

    public void locked(DoorStateMachine sm)
    {
    }
}
