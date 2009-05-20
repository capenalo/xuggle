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
