package com.xuggle.utils.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuggle.utils.event.Event;

/**
 * A generic error event.
 * 
 * @author aclarke
 *
 */

public class ErrorEvent extends Event
{
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final String mMessage;
  private final Throwable mException;

  private final IEventHandler<? extends IEvent> mHandler;

  private final IEvent mEvent;

  /**
   * Creates a new error event.
   * 
   * @param source the source object
   * @param t the exception, or null if none.
   * @param message A user defined message, or null if none
   * @param event The event that was being handled when the exception
   *   was generated.
   * @param handler The handler that the exception was thrown from, or null
   *   if none.
   */
  
  ErrorEvent(Object source,
      Throwable t,
      String message,
      IEvent event,
      IEventHandler<? extends IEvent> handler)
  {
    super(source);
    mMessage = message;
    mException = t;
    mHandler = handler;
    mEvent = event;
    log.info("Error: {}", this);
  }

  /**
   * Creates a new error event.
   * 
   * @param source the source object
   * @param t the exception, or null if none.
   * @param message A user defined message, or null if none
   * @param event The event that was being handled when the exception
   *   was generated.
   * @param handler The handler that the exception was thrown from, or null
   *   if none.
   */
  
  ErrorEvent(Object source,
      Throwable t,
      String message,
      IEvent event
  )
  {
    this(source, t, message, event, null);
  }
  
  /**
   * Creates a new error object.
   *  
   * @param source The source of the error, or null if unknown.
   * @param t A exception that goes with the error, or null if none.
   * @param message A user-defined message that goes with the error, or null
   *   if none.
   */
  
  public ErrorEvent(Object source,
      Throwable t,
      String message)
  {
    this(source, t, message, null, null);
  }

  
  /**
   * Creates a new error object.
   *  
   * @param source The source of the error, or null if unknown.
   * @param message A user-defined message that goes with the error, or null
   *   if none.
   */
  
  public ErrorEvent(Object source,
      String message)
  {
    this(source, null, message, null, null);
  }

  /**
   * Creates a new error object.
   *  
   * @param source The source of the error, or null if unknown.
   * @param t A exception that goes with the error, or null if none.
   */

  public ErrorEvent(Object source,
      Throwable t)
  {
    this(source, t, null, null, null);
  }

  /**
   * Creates a new error object.
   *  
   * @param source The source of the error, or null if unknown.
   */
  
  public ErrorEvent(Object source)
  {
    this(source, null, null, null, null);
  }

  /**
   * The user-defined message associated with this error, or null if none.
   * 
   * @return the error message.
   */
  public String getMessage()
  {
    return mMessage;
  }
  
  /**
   *
   * Returns the error event with a summary of information that might
   * be useful for inserting in a log.
   * <p>
   * We will print the first fifteen frames of the stack trace if an
   * exception is available.
   * </p>
   * 
   * @return a string
   */
  
  @Override
  public String toString()
  {
    final StringBuilder string = new StringBuilder();
    string.append(super.toString());
    string.append("[");
    string.append("source="+getSource()+";");
    final String message = getMessage();
    if (message != null && message.length()>0)
      string.append("message="+message+";");
    final IEvent event = getEvent();
    if (event != null)
      string.append("event="+event+";");
    final IEventHandler<? extends IEvent> handler = getHandler();
    if (handler!=null)
      string.append("handler="+handler+";");
    
    final Throwable t = getException();
    if (t != null)
    {
      string.append("exception="+t+";");
      StackTraceElement[] elements=t.getStackTrace();
      if (elements != null && elements.length>0)
      {
        string.append("stack trace=\n");

        int i = 0;
        for(StackTraceElement elem : elements)
        {
          string.append(elem.toString() +"\n");
          ++i;
          if (i >= 15)
            break;
        }
        string.append(";");
      }
    }
    string.append("]");
    return string.toString();
  }

  /**
   * The exception associated with this error, or null if none.
   * 
   * @return the exception
   */
  
  public Throwable getException()
  {
    return mException;
  }
  
  /**
   * The handler that the exception was thrown from, or null
   * if unknown.
   * <p>
   * If {@link #getException()} is an uncaught exception
   * encountered when processing an {@link IEventHandler}, then
   * this method returns the handler that failed to catch the exception.
   * </p>
   * 
   * @return the handler
   */
  
  public IEventHandler<? extends IEvent> getHandler()
  {
    return mHandler;
  }

  /**
   * The {@link IEvent} that was being handled when the error occurred.
   * <p>
   * If this error event was generated because of an uncaught exception
   * in an {@link IEventHandler}, then this method returns that event.
   * </p> 
   * @return the event.
   */
  public IEvent getEvent()
  {
    return mEvent;
  }

}
