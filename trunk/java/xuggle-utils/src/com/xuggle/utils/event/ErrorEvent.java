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
  
  @Override
  public String toString()
  {
    Throwable t = getException();
    StringBuilder string = new StringBuilder();
    string.append(super.toString());
    string.append("[");
    string.append("source="+getSource()+";");
    string.append("message="+getMessage()+";");
    string.append("exception="+t+";");
    if (t != null)
    {
      StackTraceElement[] elements=t.getStackTrace();
      string.append("stack trace=");
      int i = 0;
      string.append("[\n");
      for(StackTraceElement elem : elements)
      {
        string.append("frame=" + elem.toString() +";\n");
        ++i;
        if (i >= 5)
          break;
      }
      string.append("]");
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
   * 
   * @return the handler
   */
  
  public IEventHandler<? extends IEvent> getHandler()
  {
    return mHandler;
  }

  public IEvent getEvent()
  {
    return mEvent;
  }

}
