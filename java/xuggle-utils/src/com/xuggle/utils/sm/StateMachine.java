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

    private IState mState = null;

    /** The initial state of this state machine. */

    private IState mInitialState = null;

    /** The event dispatcher used by this state machine. */

    private IEventDispatcher mDispatcher;

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
      
      this.mDispatcher = (eventDispatcher == null
        ? new SynchronousEventDispatcher()
        : eventDispatcher);

      // set initial state

      this.mInitialState = initialState;
      this.mState = initialState;
    }

    /** Convenience dispatch function which just passes through to the
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
      return mDispatcher;
    }

    /** Sets the state of this state machine.  The transition will occur
     * only after the state machine achieves quiescence.
     *
     * @param target the target state to transition to
     */

    public void setState(IState target)
    {
      IState from = mState;
      mState = target;
      log.debug("State change: " + from + " -> " + mState);
      IEvent event = newTransitionEvent(this, from, target);
      mDispatcher.dispatchEvent(event);
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
      return mState;
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
      mDispatcher.dispatchEvent(new EventDispatcherAbortEvent(this));
      
      // if event dispatcher is asynchronous, then wait for it to shut
      // down, then null the state, and then restart it

      if (mDispatcher instanceof AsynchronousEventDispatcher)
      {
        AsynchronousEventDispatcher aed = 
          (AsynchronousEventDispatcher)mDispatcher;
        aed.waitForDispatcherToFinish(timeout);
        mState = null;
        aed.startDispatching();
      }

      // owtherwise just null the state

      else
        mState = null;

      // dispatch transition to initial state

      setState(mInitialState);
    }

    /**
     * This event is fired whenever a state machine transitions from one state to another.
     */
    public static class TransitionEvent extends Event
    {
        /** Target state of this transition event. */

        private IState mFrom;
        private IState mTo;

        /** Construct a transition event
         *
         * @param source source of event
         * @param from state transitioning from
         * @param to state transitioning to
         */

        public TransitionEvent(Object source, IState from, IState to)
        {
          super(source);
          this.mFrom = from;
          this.mTo = to;
        }

        /** Get the state that the sm is transitioning from.
         *
         * @return the state the sm is transitioning from.
         */
        
        public IState getFrom()
        {
          return mFrom;
        }

        /** Get the state that the is sm transitioning to.
         *
         * @return the state the sm is transitioning to.
         */
        
        public IState getTo()
        {
          return mTo;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Key addEventHandler(int priority,
        Class<? extends IEvent> eventClass,
        IEventHandler<? extends IEvent> handler)
    {
      return getEventDispatcher().addEventHandler(priority, eventClass, handler);
    }

    /**
     * {@inheritDoc}
     */
    public void removeEventHandler(Key key)
        throws IndexOutOfBoundsException
    {
      getEventDispatcher().removeEventHandler(key);
    }

    /**
     * {@inheritDoc}
     */
    public Key addEventHandler(int priority,
        Class<? extends IEvent> eventClass,
        IEventHandler<? extends IEvent> handler, boolean useWeakReferences)
    {
      return getEventDispatcher().addEventHandler(priority, eventClass, handler,
          useWeakReferences);
    }
}
