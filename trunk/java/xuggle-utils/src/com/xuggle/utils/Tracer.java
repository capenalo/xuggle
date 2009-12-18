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
  /**
   * Implementation note: For performance reasons Tracer's SHOULD
   * be 100% immutable and should pre-compute values if cheap to
   * do so.  Trust me on that.
   */
  final private UUID mID;
  final private Object mSource;
  final private Object mMessage;
  final private TimeValue mTimeStamp;
  final private long mElapsedNanoSeconds;
  final private TimeValue mElapsedTime;
  final private Tracer mParent;
  
  private Tracer(Object source, Object message, TimeValue timestamp, Tracer parent)
  {
    if (source == null)
      throw new NullPointerException();
    mSource = source;
    mMessage = message;
    if (timestamp == null)
      timestamp = TimeValue.nanoNow();
    mTimeStamp = timestamp;
    if (parent == null) {
      mID = UUID.randomUUID();
      mParent = null;
      mElapsedNanoSeconds = 0;
    } else {
      mID = parent.getID();
      mParent = parent;
      mElapsedNanoSeconds = timestamp.get(TimeUnit.NANOSECONDS)
        - mParent.getTimeStamp().get(TimeUnit.NANOSECONDS)
        + mParent.mElapsedNanoSeconds;
    }
    mElapsedTime = new TimeValue(mElapsedNanoSeconds, TimeUnit.NANOSECONDS);
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
   * Return the ancestor of a given tracer which matches a specific source
   * object.
   * 
   * @param source the source object to match against
   * @return the found ancestor or null of no matching ancestor found
   */

  public Tracer findAncestor(Object source)
  {
    Tracer current = this;
    while (null != current)
      if (current.getSource() == source)
        return current;
      else
        current = current.getParent();

    return null;
  }
  
  /**
   * Return the original tracer of this tracer.
   * 
   * @param source the source object to match against
   * @return the found ancestor or null of no matching ancestor found
   */

  public Tracer findOriginator()
  {
    Tracer current = this;
    
    while (null != current.getParent())
      current = current.getParent();

    return current;
  }

  /**
   * Compute the difference between this tracer and the one passed in. This
   * function performs a simple subtraction between the time stamps: passed -
   * this.
   * 
   * @param the the tracer to who's timestamp will be subtract from this
   *        tracers time stamp.
   * @param unit the unit of the desired result
   * @return the difference between this tracer and the passed tracers time
   *         stamps.
   */

  public long difference(Tracer tracer, TimeUnit unit)
  {
    return difference(this, tracer, unit);
  }
  
  /**
   * Compute the difference between two tracer's time stamps. This function
   * performs a simple subtraction between the time stamps: b - a.
   * 
   * @param a the first tracer 
   * @param b the second tracer
   * @param unit the unit of the desired result
   * @return the difference between the two tracers time stamps.
   */

  public static long difference(Tracer a, Tracer b, TimeUnit unit)
  {
    return b.getTimeStamp().get(unit) - a.getTimeStamp().get(unit);
  }
  
  /**
   * Creates an XML string version of the tracer.
   * <p>
   * Time stamps are printed as {@link TimeUnit#NANOSECONDS} unless otherwise specified
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
    
    final TimeUnit unit = mTimeStamp.getUnit();
    if (!TimeUnit.NANOSECONDS.equals(unit)) {
      builder.append(" unit=\"");
      builder.append(mTimeStamp.getUnit());
      builder.append("\"");
    }
    builder.append(" elapsed=\"");
    builder.append(mElapsedTime.getValue());
    builder.append("\"");
    
    final TimeUnit elapsedunit = mElapsedTime.getUnit();
    if (!TimeUnit.NANOSECONDS.equals(elapsedunit)) {
      builder.append(" elapsedunit=\"");
      builder.append(mElapsedTime.getUnit());
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

  /**
   * Get the time that has elapsed since the first instance
   * of this {@link Tracer}.
   * @return The time elapsed.
   */
  public TimeValue getElapsedTime()
  {
    return mElapsedTime;
  }
  
}
