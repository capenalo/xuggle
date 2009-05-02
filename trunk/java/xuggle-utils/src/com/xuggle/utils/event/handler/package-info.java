/**
 * Provides special implementations of
 * {@link com.xuggle.utils.event.IEventHandler}s.
 * <p>
 * There are certain types of {@link com.xuggle.utils.event.IEventHandler}
 * implementations that are very common.  For example, sometimes
 * you want to forward an event from one
 * {@link com.xuggle.utils.event.IEventDispatcher}
 * to another.
 * Sometimes you only want
 * a {@link com.xuggle.utils.event.IEventHandler} to execute if the
 * {@link com.xuggle.utils.event.IEvent#getSource()} is equal to
 * a given source.
 * Sometimes you only
 * want to handler to execute a maximum number of times.
 * </p>
 * <p>
 * This class tries to provide some of those implementations for you.
 * </p>
 * <p>
 * Use the com.xuggle.utils.event.handler.Handler class to find
 * Factory methods for the special handlers you want.
 * </p>
 * @see com.xuggle.utils.event.handler.Handler
 * 
 */
package com.xuggle.utils.event.handler;
