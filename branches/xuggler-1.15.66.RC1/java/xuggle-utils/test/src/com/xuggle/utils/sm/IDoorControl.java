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

import com.xuggle.utils.event.Event;

public interface IDoorControl
{
    /** Open the door. */
    
    public void open();

    /** Close the door. */
    
    public void close();

    /** Unlock the door. 
     *
     * @param hasKey true if you have the key at the time of the  unlocking
     */

    public void unlock(boolean hasKey);

    /** Lock the door. 
     *
     * @param hasKey true if you have the key at the time of the  unlocking
     */
    
    public void lock(boolean hasKey);

    /** Class from which all door events are derived. */

    public class DoorEvent extends Event
    {
        public DoorEvent(Object source)
        {
          super(source);
        }
    }

    /** The open event. */

    public class OpenEvent extends DoorEvent
    {
        public OpenEvent(Object source)
        {
          super(source);
        }
    }

    /** The opened event. */

    public class OpenedEvent extends DoorEvent
    {
        public OpenedEvent(Object source)
        {
          super(source);
        }
    }

    /** The close event. */

    public class CloseEvent extends DoorEvent
    {
        public CloseEvent(Object source)
        {
          super(source);
        }
    }

    /** The closed event. */

    public class ClosedEvent extends DoorEvent
    {
        public ClosedEvent(Object source)
        {
          super(source);
        }
    }

    /** The lock event. */

    public class LockEvent extends DoorEvent
    {
        private boolean hasKey;

        public LockEvent(Object source, boolean hasKey)
        {
          super(source);
          this.hasKey = hasKey;
        }

        public boolean hasKey()
        {
          return hasKey;
        }
    }

    /** The locked event. */

    public class LockedEvent extends DoorEvent
    {
        public LockedEvent(Object source)
        {
          super(source);
        }
    }

    /** The unlock event. */

    public class UnlockEvent extends DoorEvent
    {
        private boolean hasKey;

        public UnlockEvent(Object source, boolean hasKey)
        {
          super(source);
          this.hasKey = hasKey;
        }

        public boolean hasKey()
        {
          return hasKey;
        }
    }
}
