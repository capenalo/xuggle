package com.xuggle.utils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * An object that can be used to trace an ID as it moves through time
 * and is 'stamped' by various object consumers.
 * <p>
 * Each tracer has a unique {@link UUID} associated with it, but may
 * have many instances as it propagates through a system.  Users either
 * create a new tracer, or get 'propagatable' instances by calling {@link #stamp(Object, Object, TimeValue)}.
 * </p>
 * @author aclarke
 *
 */
public class Tracer
{
  final private UUID mID;
  final private Object mSource;
  final private Object mMessage;
  final private TimeValue mTimeStamp;
  final private Tracer mParent;
  
  private Tracer(Object source, Object message, TimeValue timestamp, Tracer parent)
  {
    if (source == null)
      throw new NullPointerException();
    if (parent == null) {
      mID = UUID.randomUUID();
      mParent = null;
    } else {
      mID = parent.getID();
      mParent = parent;
    }
    mSource = source;
    mMessage = message;
    if (timestamp == null)
      timestamp = TimeValue.nanoNow();
    mTimeStamp = timestamp;
  }
  
  /**
   * Create a new tracer.
   * @param source The source creating the tracer.  May not be null.
   * @param message An optional message; may be null.
   * @param timestamp An optional timestamp.  If null {@link TimeValue#nanoNow()}
   *   is used.
   * @throws NullPointerException if source == null
   */
  public Tracer(Object source, Object message, TimeValue timestamp)
  {
    this(source, message, timestamp, null);
  }
  
  /**
   * Create a new tracer.
   * @param source The source creating the tracer.  May not be null.
   * @param message An optional message; may be null.
   * @throws NullPointerException if source == null
   */
  public Tracer(Object source, Object message)
  {
    this(source, message, null, null);
  }
  
  /**
   * Create a new tracer.
   * @param source The source creating the tracer.  May not be null.
   * @throws NullPointerException if source == null
   */
  public Tracer(Object source)
  {
    this(source, null, null, null);
  }
  
  /**
   * Creates a new tracer based on this tracer, but stamped with the new
   * source and message.
   * 
   * @param source The source stamping this tracer; must be non null.
   * @param message An optional message.  May be null.
   * @param timestamp A time stamp.  If null, {@link TimeValue#nanoNow()} is used.
   * @return A new tracer that can be used for propagation.
   */
  public Tracer stamp(Object source, Object message, TimeValue timestamp)
  {
    return new Tracer(source, message, timestamp, this);
  }
  /**
   * Creates a new tracer based on this tracer, but stamped with the new
   * source and time stamp.
   * 
   * @param source The source stamping this tracer; must be non null.
   * @param timestamp A time stamp.  If null, {@link TimeValue#nanoNow()} is used.
   * @return A new tracer that can be used for propagation.
   */
  public Tracer stamp(Object source, TimeValue timestamp)
  {
    return this.stamp(source, null, timestamp);
  }
  /**
   * Creates a new tracer based on this tracer, but stamped with the new
   * source.
   * 
   * @param source The source stamping this tracer; must be non null.
   * @return A new tracer that can be used for propagation.
   */
  public Tracer stamp(Object source)
  {
    return this.stamp(source, null, null);
  }
  
  /**
   * The unique ID of this tracer.  Can be used to correlated different
   * {@link Tracer} instances.
   * @return The unique id of this tracer.
   */
  public UUID getID()
  {
    return mID;
  }
  /**
   * The source object that created this instance of the tracer.
   * @return the source
   */
  public Object getSource()
  {
    return mSource;
  }
  /**
   * The timestamp associated with this instance of the tracer.
   * @return the timestamp.
   */
  public TimeValue getTimeStamp()
  {
    return mTimeStamp;
  }
  
  /**
   * The message associated with this instance, or null if none.
   * @return The message.
   */
  public Object getMessage()
  {
    return mMessage;
  }
  /**
   * The previous instance of this tracer, or null if this is the first
   * instance.
   * @return The previous instance.
   */
  public Tracer getParent()
  {
    return mParent;
  }
  
  /**
   * Creates an XML string version of the tracer.
   * <p>
   * Time stamps are printed as {@link TimeUnit#MICROSECONDS} unless otherwise specified
   * with a unit attribute. 
   * </p>
   * @return An XML string.
   */
  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder();

    builder.append("<tracer");
    builder.append(" id=\"");
    builder.append(mID);
    builder.append("\"");
    builder.append(" instance=\"");
    builder.append(this.hashCode());
    builder.append("\"");

    builder.append(" ts=\"");
    builder.append(mTimeStamp.getValue());
    builder.append("\"");
    
    TimeUnit unit = mTimeStamp.getUnit();
    if (!TimeUnit.MICROSECONDS.equals(unit)) {
      builder.append(" unit=\"");
      builder.append(mTimeStamp.getUnit());
      builder.append("\"");
    }
    builder.append(">");
    builder.append("<source>");
    builder.append(mSource);
    builder.append("</source>");
    if (mMessage != null)
    {
      builder.append("<message>");
      builder.append(mMessage);
      builder.append("</message>");
    }
    if (mParent != null)
    {
      //FIXME: Should do loop detection here
      builder.append(mParent);
    }
    builder.append("</tracer>");
    return builder.toString();
  }
}
