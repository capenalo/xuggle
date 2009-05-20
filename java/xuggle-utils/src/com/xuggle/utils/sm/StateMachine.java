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

import com.xuggle.utils.event.AsynchronousEventDispatcher;
import com.xuggle.utils.event.Event;
import com.xuggle.utils.event.EventDispatcherAbortEvent;
import com.xuggle.utils.event.IAsynchronousEventDispatcher;
import com.xuggle.utils.event.IEvent;
import com.xuggle.utils.event.IEventDispatcher;
import com.xuggle.utils.event.IEventHandlerRegistrable;
import com.xuggle.utils.event.IEventHandler;
import com.xuggle.utils.event.SynchronousEventDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract base class for State Machines to use.
 */
public class StateMachine implements IEventHandlerRegistrable
{
    /** The logger. */

    static Logger log = LoggerFactory.getLogger(StateMachine.class);

    /** Current state of this state machine. */

    private IState state = null;

    /** The initial state of this state machine. */

    private IState initialState = null;

    /** The event dispatcher used by this state machine. */

    private IEventDispatcher eventDispatcher;

    /** Construct a state machine.
     *
     * @param eventDispatcher the dispatcher which will handle events
     * for this state machine, if null the state machine will create
     * it's own SynchronousEventDispatcher
     * @param initialState the state this state machine will initialized
     * to (or reset to)
     */ 
    public StateMachine(
      IEventDispatcher eventDispatcher, IState initialState)
    {
      // if a dispatcher has been passed us that otherwise create a
      // Synchronouseventdispatcher internally
      
      this.eventDispatcher = (eventDispatcher == null
        ? new SynchronousEventDispatcher()
        : eventDispatcher);

      // set initial state

      this.initialState = initialState;
      this.state = initialState;
    }

    /** Convience dipsatch function which just passes through to the
     * internal event dispatcher.
     * 
     * @param event The event to dispatch
     */

    public void dispatchEvent(IEvent event)
    {
      getEventDispatcher().dispatchEvent(event);
    }

    /** Get the event dispatcher being used by this state machine.
     *
     * @return the dispatcher used by this state machine.
     */

    public IEventDispatcher getEventDispatcher()
    {
      return eventDispatcher;
    }

    /** Sets the state of this state machine.  The transition will occur
     * only after the state machine achieves quiescence.
     *
     * @param target the target state to transition to
     */

    public void setState(IState target)
    {
      IState from = state;
      state = target;
      log.debug("State change: " + from + " -> " + state);
      IEvent event = newTransitionEvent(this, from, target);
      eventDispatcher.dispatchEvent(event);
    }

    /**
     * Override in child classes if you want to provide your own transition
     * event.
     * 
     * @param stateMachine The source state machine
     * @param from The state we're transitioning from
     * @param target The state we're transitioning to
     * @return The new event
     */
    protected IEvent newTransitionEvent(StateMachine stateMachine,
        IState from, IState target)
    {
      return new TransitionEvent(stateMachine, from, target);
    }

    /**
     * Return the current state of the state machine 
     * @return The state
     */
    
    public IState getState()
    {
      return state;
    }

    /**
     * Reset this state machine to it's initial state.
     * If we're using an {@link IAsynchronousEventDispatcher}, this will
     * stop it, and then restart it as well.
     *   
     * @param timeout duration in miliseconds to wait for an
     *  {@link IAsynchronousEventDispatcher} to finish.  Ignored if we're not
     *  using an {@link IAsynchronousEventDispatcher} object.
     */

    public void reset(long timeout)
    {
      eventDispatcher.dispatchEvent(new EventDispatcherAbortEvent(this));
      
      // if event dispatcher is asynchronous, then wait for it to shut
      // down, then null the state, and then restart it

      if (eventDispatcher instanceof AsynchronousEventDispatcher)
      {
        AsynchronousEventDispatcher aed = 
          (AsynchronousEventDispatcher)eventDispatcher;
        aed.waitForDispatcherToFinish(timeout);
        state = null;
        aed.startDispatching();
      }

      // owtherwise just null the state

      else
        state = null;

      // dispatch transition to initial state

      setState(initialState);
    }

    /**
     * This event is fired whenever a state machine transitions from one state to another.
     */
    public static class TransitionEvent extends Event
    {
        /** Target state of this transition event. */

        private IState from;
        private IState to;

        /** Construct a transition event
         *
         * @param source source of event
         * @param from state transitioning from
         * @param to state transitioning to
         */

        public TransitionEvent(Object source, IState from, IState to)
        {
          super(source);
          this.from = from;
          this.to = to;
        }

        /** Get the state that the sm is transitioning from.
         *
         * @return the state the sm is transitioning from.
         */
        
        public IState getFrom()
        {
          return from;
        }

        /** Get the state that the is sm transitioning to.
         *
         * @return the state the sm is transitioning to.
         */
        
        public IState getTo()
        {
          return to;
        }
    }

    public void addEventHandler(int priority,
        Class<? extends IEvent> eventClass,
        IEventHandler<? extends IEvent> handler)
    {
      getEventDispatcher().addEventHandler(priority, eventClass, handler);
    }

    public void removeEventHandler(int priority,
        Class<? extends IEvent> eventClass,
        IEventHandler<? extends IEvent> handler)
        throws IndexOutOfBoundsException
    {
      getEventDispatcher().removeEventHandler(priority, eventClass, handler);
    }

    public void addEventHandler(int priority,
        Class<? extends IEvent> eventClass,
        IEventHandler<? extends IEvent> handler, boolean useWeakReferences)
    {
      getEventDispatcher().addEventHandler(priority, eventClass, handler,
          useWeakReferences);
    }
}
