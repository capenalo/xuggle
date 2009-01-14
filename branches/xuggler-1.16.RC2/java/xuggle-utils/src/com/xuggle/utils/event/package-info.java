/**
 * Provides a generalized event-passing mechanism similar to ActionScript's
 * model.
 * <p>
 * Provides two implementations of an event dispatcher:
 * <ul>
 * <li>{@link com.xuggle.utils.event.SynchronousEventDispatcher}: Dispatches events on the current thread.</li>
 * <li>{@link com.xuggle.utils.event.AsynchronousEventDispatcher}: Bundles up the event and dispatches on a dispatcher thread.</li>
 * </ul>
 *
 */
package com.xuggle.utils.event;

