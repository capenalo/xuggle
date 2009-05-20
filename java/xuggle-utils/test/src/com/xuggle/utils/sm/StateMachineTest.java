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

import static com.xuggle.utils.sm.DoorStateMachine.*;
import static junit.framework.Assert.assertTrue;

import com.xuggle.utils.event.AsynchronousEventDispatcher;
import com.xuggle.utils.event.IEvent;
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.IEventHandler;
import com.xuggle.utils.sm.IState;
import com.xuggle.utils.sm.State;
import com.xuggle.utils.sm.StateMachine.TransitionEvent;

import org.junit.runners.Parameterized;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@RunWith(Parameterized.class)
public class StateMachineTest
{
    static Logger log = LoggerFactory.getLogger(StateMachineTest.class);

    // return the parameterized test cases

    @Parameterized.Parameters
    public static java.util.Vector<Object[]> getParameters()
    {
      java.util.Vector<Object[]> cases = new java.util.Vector<Object[]>();
      cases.add(new Object[] {true});
      cases.add(new Object[] {false});
      return cases;
    }

    // the state machine

    DoorStateMachine sm1;

    // this seccondd state machine exists to test that two state
    // machines can coexist gracefully (turns out they didn't initially)

    DoorStateMachine sm2;

    // test with a SynchronousEventDispatcher or
    // AsynchronousEventDispatcher

    boolean isAsyncTest;

    public StateMachineTest(boolean isAsyncTest)
    {
      this.isAsyncTest = isAsyncTest;
    }

    @Before
    public void setup()
    {
      log.debug("Running test: {}", " ----------- ");
      //NameAwareTestClassRunner.getTestMethodName());

      if (isAsyncTest)
      {
        AsynchronousEventDispatcher aed = new AsynchronousEventDispatcher();
        sm1 = new DoorStateMachine(aed);
        sm2 = new DoorStateMachine(aed);
        aed.startDispatching();
      }
      else
      {
        sm1 = new DoorStateMachine();
        sm2 = new DoorStateMachine();
      }
    }

    @Test
    public void initalStateTest()
    {
      assertState(CLOSED);
    }

    @Test(timeout=1000)
    public void succeedtransitionTest()
    {
      Watcher w1 = addWatcher(OPENING, OPEN);
      sm1.open();
      waitForWatcher(w1);
    }

    @Test(timeout=1000)
    public void failedTransitonTest()
    {
      Watcher w1 = addWatcher(OPENING, OPEN);
      sm1.open();
      waitForWatcher(w1);
      sm1.lock(true);
      assertState(OPEN);
    }

    @Test(timeout=1000)
    public void conditionalFailTransitonTest()
    {
      Watcher w1 = addWatcher(LOCKING, CLOSED);
      sm1.lock(false);
      waitForWatcher(w1);
    }

    @Test(timeout=1000)
    public void conditionalSucceedTransitonTest()
    {
      Watcher w1 = addWatcher(LOCKING, LOCKED);
      sm1.lock(true);
      waitForWatcher(w1);
    }

    @Test(timeout=1000)
    public void resetTest()
    {
      Watcher w1 = addWatcher(null, CLOSED);
      sm1.reset(0);
      waitForWatcher(w1);
    }


    @Test(timeout=2000)
    public void allStatesNominalTest()
    {
      Watcher w1;
      Watcher w2;
        
      w1 = addWatcher(CLOSED, LOCKING);
      w2 = addWatcher(LOCKING, LOCKED);
      sm1.lock(true);
      waitForWatcher(w1);
      waitForWatcher(w2);

      w1 = addWatcher(LOCKED, UNLOCKING);
      w2 = addWatcher(UNLOCKING, CLOSED);
      sm1.unlock(true);
      waitForWatcher(w1);
      waitForWatcher(w2);

      w1 = addWatcher(CLOSED, OPENING);
      w2 = addWatcher(OPENING, OPEN);
      sm1.open();
      waitForWatcher(w1);
      waitForWatcher(w2);
      
      w1 = addWatcher(OPEN, CLOSING);
      w2 = addWatcher(CLOSING, CLOSED);
      sm1.close();
      waitForWatcher(w1);
      waitForWatcher(w2);
    }

    @Test(timeout=2000)
    public void thingsYouCantDoTest()
    {
      Watcher w;

      // close a closed door
      
      sm1.close();
      assertState(CLOSED);

       // unlock an unlocked (closed) door

      sm1.unlock(true);
      assertState(CLOSED);

       // open the door

      w = addWatcher(OPENING, OPEN);
      sm1.open();
      waitForWatcher(w);
      assertState(OPEN);

       // and open the door again

       sm1.open();
       assertState(OPEN);
    }

    // assert the state of the door

    public void assertState(State state)
    {
      assertTrue(
        "state of the door should be " + state + " but it is " 
        + sm1.getState() + " instead",
        sm1.getState() == state);
    }

    // this adds a watcher to the state machine looking for a partular
    // state tranistion condition, and tells the watcher when it is found

    public Watcher addWatcher(DoorStateAdapter from, DoorStateAdapter to)
    {
      final Watcher w = new Watcher(from, to);

      w.handler = new IEventHandler<IEvent>()
        {
            
            public boolean handleEvent(IEventDispatcher dispatcher, IEvent event)
            {
              TransitionEvent te = (TransitionEvent)event;
              IState from = te.getFrom();
              IState to = te.getTo();
              log.debug("[" + w.id + "] Watched: " + from + " -> " + to + ".");
              if (w.from == from && w.to == to)
                w.found = true;
              return false;
            }
        };
      
      sm1.getEventDispatcher().addEventHandler(
        0, TransitionEvent.class, w.handler);

      return w;
    }

    // wait for a given watcher to have it's condition found

    public void waitForWatcher(Watcher w)
    {
      try
      {
        // sleep while target not found

        while (!w.found)
          Thread.sleep(10);

        // when remove the handler

        sm1.getEventDispatcher().removeEventHandler(
          0, TransitionEvent.class, w.handler);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }

    private static int watcherCounter = 0;

    class Watcher
    {
        public int id = watcherCounter++;

        public State         to;
        public State         from;
        public boolean       found = false;
        public IEventHandler<IEvent> handler;

        Watcher(State from, State to)
        {
          this.from = from;
          this.to = to;
        }
    }
}
