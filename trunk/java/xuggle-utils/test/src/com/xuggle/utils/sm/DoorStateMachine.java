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


import com.xuggle.test_utils.NameAwareTestClassRunner;
import com.xuggle.utils.event.IEvent;
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.IEventHandler;
import com.xuggle.utils.sm.StateMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.runner.RunWith;


/** Example state machine implementation for a door which can be opened,
 * closed, locked and unlocked. locking and unlocking can fail in the
 * absense of a key.
 */

@RunWith(NameAwareTestClassRunner.class)
class DoorStateMachine extends StateMachine implements IDoorControl,
IEventHandler<IEvent>
{
    static Logger log = LoggerFactory.getLogger(DoorStateMachine.class);

    // terminal states

    public static final DoorStateAdapter CLOSED = new DoorStateAdapter("CLOSED")
      {
           public void open(DoorStateMachine sm)
          {
            log.debug("CLOSED.open()");
            sm.setState(OPENING);
            sm.opened();
          }

           public void lock(DoorStateMachine sm, boolean hasKey)
          {
            log.debug("CLOSED.lock()");
            sm.setState(LOCKING);
            if (hasKey)
              sm.locked();
            else
              sm.closed();
          }
      };

    public static final DoorStateAdapter OPEN = new DoorStateAdapter("OPEN")
      {
           public void close(DoorStateMachine sm)
          {
            log.debug("OPEN.close()");
            sm.setState(CLOSING);
            sm.closed();
          }
      };
    
    public static final DoorStateAdapter LOCKED = new DoorStateAdapter("LOCKED")
      {
           public void unlock(DoorStateMachine sm, boolean hasKey)
          {
            log.debug("LOCKED.unlock()");
            sm.setState(UNLOCKING);
            if (hasKey)
              sm.closed();
            else
              sm.locked();
          }
      };
    
    // transitioning states

    public static final DoorStateAdapter LOCKING = new DoorStateAdapter("LOCKING")
      {
           public void locked(DoorStateMachine sm)
          {
            log.debug("LOCKING.locked()");
            sm.setState(LOCKED);
          }

           public void closed(DoorStateMachine sm)
          {
            log.debug("LOCKING.closed()");
            sm.setState(CLOSED);
          }
      };
    
    public static final DoorStateAdapter UNLOCKING = new DoorStateAdapter("UNLOCKING")
      {
           public void closed(DoorStateMachine sm)
          {
            log.debug("CLOSING.closed()");
            sm.setState(CLOSED);
          }
      };
    
    public static final DoorStateAdapter OPENING = new DoorStateAdapter("OPENING")
      {
           public void opened(DoorStateMachine sm)
          {
            log.debug("OPENING.opened()");
            sm.setState(OPEN);
          }
      };

    public static final DoorStateAdapter CLOSING = new DoorStateAdapter("CLOSING")
      {
           public void closed(DoorStateMachine sm)
          {
            log.debug("CLOSING.closed()");
            sm.setState(CLOSED);
          }
      };

    // make me a door state machine with the default event dispatcher
    
    public DoorStateMachine()
    {
      this(null);
    }

    // make me a door state machine
    
    public DoorStateMachine(IEventDispatcher eventDispatcher)
    {
      super(eventDispatcher, CLOSED);

      // add self as handler of dispatch events
      
      getEventDispatcher().addEventHandler(0, OpenEvent  .class, this);
      getEventDispatcher().addEventHandler(0, OpenedEvent.class, this);
      getEventDispatcher().addEventHandler(0, CloseEvent .class, this);
      getEventDispatcher().addEventHandler(0, ClosedEvent.class, this);
      getEventDispatcher().addEventHandler(0, UnlockEvent.class, this);
      getEventDispatcher().addEventHandler(0, LockEvent  .class, this);
      getEventDispatcher().addEventHandler(0, LockedEvent.class, this);
      getEventDispatcher().addEventHandler(0, DoorEvent  .class, this);
    }
    
    // open the door

     public void open()
    {
      dispatchEvent(new OpenEvent(this));
    }

    // note that the door is open

    private void opened()
    {
      dispatchEvent(new OpenedEvent(this));
    }

    // close the door

     public void close()
    {
      dispatchEvent(new CloseEvent(this));
    }

    // note that the door is closed

    private void closed()
    {
      dispatchEvent(new ClosedEvent(this));
    }

    // unlock the door

     public void unlock(boolean hasKey)
    {
      dispatchEvent(new UnlockEvent(this, hasKey));
    }

    // locking the door

     public void lock(boolean hasKey)
    {
      dispatchEvent(new LockEvent(this, hasKey));
    }

    // note that the door is locked

    private void locked()
    {
      dispatchEvent(new LockedEvent(this));
    }

    // handle events

    
    public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
    {
      log.debug("handleEvent(): " + event);

      // handle open event

      if (event instanceof OpenEvent)
      {
        getState().open(this);
        return true;
      }
        
      // handle opened event

      if (event instanceof OpenedEvent)
      {
        getState().opened(this);
        return true;
      }
        
      // handle close event

      if (event instanceof CloseEvent)
      {
        getState().close(this);
        return true;
      }
        
      // handle closed event

      if (event instanceof ClosedEvent)
      {
        getState().closed(this);
        return true;
      }
        
      // handle lock event

      if (event instanceof LockEvent)
      {
        getState().lock(this, ((LockEvent)event).hasKey());
        return true;
      }
        
      // handle locked event

      if (event instanceof LockedEvent)
      {
        getState().locked(this);
        return true;
      }
        
      // handle unlock event

      if (event instanceof UnlockEvent)
      {
        getState().unlock(this, ((UnlockEvent)event).hasKey());
        return true;
      }
        
      // the event was not handled

      return false;
    }

    /** Override get state to return the door state adapter type, makes
     * live a bit easier.
     *
     * @return the current state as a DoorStateAdapter;
     */

    public final DoorStateAdapter getState()
    {
      return (DoorStateAdapter)(super.getState());
    }
}
