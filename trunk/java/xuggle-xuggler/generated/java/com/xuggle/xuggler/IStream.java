/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.0
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.xuggle.xuggler;
import com.xuggle.ferry.*;
/**
 * Represents a stream of similar data (eg video) in a {@link IContainer}. 
 *  
 * <p>  
 * Streams are really virtual concepts; {@link IContainer}s really just 
 * contain  
 * a bunch of {@link IPacket}s. But each {@link IPacket} usually has 
 * a stream  
 * id associated with it, and all {@link IPacket}s with that stream 
 * id represent  
 * the same type of (usually time-based) data. For example in many FLV 
 *  
 * video files, there is a stream with id "0" that contains all video 
 * data, and  
 * a stream with id "1" that contains all audio data.  
 * </p><p>  
 * You use an {@link IStream} object to get properly configured {@link 
 * IStreamCoder}  
 * for decoding, and to tell {@link IStreamCoder}s how to encode {@link 
 * IPacket}s when  
 * decoding.  
 * </p>  
 */
public class IStream extends RefCounted {
  // JNIHelper.swg: Start generated code
  // >>>>>>>>>>>>>>>>>>>>>>>>>>>
  /**
   * This method is only here to use some references and remove
   * a Eclipse compiler warning.
   */
  @SuppressWarnings("unused")
  private void noop()
  {
    IBuffer.make(null, 1);
  }
   
  private volatile long swigCPtr;

  /**
   * Internal Only.
   */
  protected IStream(long cPtr, boolean cMemoryOwn) {
    super(XugglerJNI.SWIGIStreamUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }
  
  /**
   * Internal Only.
   */
  protected IStream(long cPtr, boolean cMemoryOwn,
      java.util.concurrent.atomic.AtomicLong ref)
  {
    super(XugglerJNI.SWIGIStreamUpcast(cPtr),
     cMemoryOwn, ref);
    swigCPtr = cPtr;
  }
    
  /**
   * Internal Only.  Not part of public API.
   *
   * Get the raw value of the native object that obj is proxying for.
   *   
   * @param obj The java proxy object for a native object.
   * @return The raw pointer obj is proxying for.
   */
  public static long getCPtr(IStream obj) {
    if (obj == null) return 0;
    return obj.getMyCPtr();
  }

  /**
   * Internal Only.  Not part of public API.
   *
   * Get the raw value of the native object that we're proxying for.
   *   
   * @return The raw pointer we're proxying for.
   */  
  public long getMyCPtr() {
    if (swigCPtr == 0) throw new IllegalStateException("underlying native object already deleted");
    return swigCPtr;
  }
  
  /**
   * Create a new IStream object that is actually referring to the
   * exact same underlying native object.
   *
   * @return the new Java object.
   */
  @Override
  public IStream copyReference() {
    if (swigCPtr == 0)
      return null;
    else
      return new IStream(swigCPtr, swigCMemOwn, getJavaRefCount());
  }

  /**
   * Compares two values, returning true if the underlying objects in native code are the same object.
   *
   * That means you can have two different Java objects, but when you do a comparison, you'll find out
   * they are the EXACT same object.
   *
   * @return True if the underlying native object is the same.  False otherwise.
   */
  public boolean equals(Object obj) {
    boolean equal = false;
    if (obj instanceof IStream)
      equal = (((IStream)obj).swigCPtr == this.swigCPtr);
    return equal;
  }
  
  /**
   * Get a hashable value for this object.
   *
   * @return the hashable value.
   */
  public int hashCode() {
     return (int)swigCPtr;
  }
  
  // <<<<<<<<<<<<<<<<<<<<<<<<<<<
  // JNIHelper.swg: End generated code
  

  /**
   * info about this stream
   * @return information about this stream
   */
   
  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder();
    
    result.append(this.getClass().getName()+"@"+hashCode()+"[");
    result.append("index:"+getIndex()+";");
    result.append("id:"+getId()+";");
    result.append("streamcoder:"+getStreamCoder()+";");
    result.append("framerate:"+getFrameRate()+";");
    result.append("timebase:"+getTimeBase()+";");
    result.append("direction:"+getDirection()+";");
    result.append("]");
    return result.toString();
  }

  /**
   * Get an ordered sequence of index entries in this {@link IStream}.
   * 
   * @return A list of entries.  Will always return a non-null
   *   list, but if there are no entries the list size will be zero.
   */
  public java.util.List<IIndexEntry> getIndexEntries()
  {
    final int numEntries = getNumIndexEntries();
    java.util.List<IIndexEntry> retval = new java.util.ArrayList<IIndexEntry>(Math.max(numEntries, 10));
    for(int i = 0; i < numEntries; i++) {
      final IIndexEntry entry = getIndexEntry(i);
      if (entry != null) {
       retval.add(entry); 
      }
    }
    
    return retval;
  }


/**
 * Get the {@link Direction} this stream is pointing in.  
 * @return	The direction of this stream.  
 */
  public IStream.Direction getDirection() {
    return IStream.Direction.swigToEnum(XugglerJNI.IStream_getDirection(swigCPtr, this));
  }

/**
 * Get the relative position this stream has in the hosting  
 * {@link IContainer} object.  
 * @return	The Index within the Container of this stream.  
 */
  public int getIndex() {
    return XugglerJNI.IStream_getIndex(swigCPtr, this);
  }

/**
 * Return a container format specific id for this stream.  
 * @return	The (container format specific) id of this stream.  
 */
  public int getId() {
    return XugglerJNI.IStream_getId(swigCPtr, this);
  }

/**
 * Get the StreamCoder than can manipulate this stream.  
 * If the stream is an INBOUND stream, then the StreamCoder can  
 * do a IStreamCoder::DECODE. IF this stream is an OUTBOUND stream, 
 *  
 * then the StreamCoder can do all IStreamCoder::ENCODE methods.  
 * @return	The StreamCoder assigned to this object.  
 */
  public IStreamCoder getStreamCoder() {
    long cPtr = XugglerJNI.IStream_getStreamCoder(swigCPtr, this);
    return (cPtr == 0) ? null : new IStreamCoder(cPtr, false);
  }

/**
 * Get the (sometimes estimated) frame rate of this container.  
 *  
 * an approimation. Better to use getTimeBase().  
 * For contant frame-rate containers, this will be 1 ( getTimeBase() 
 * )  
 * @return	The frame-rate of this container.  
 */
  public IRational getFrameRate() {
    long cPtr = XugglerJNI.IStream_getFrameRate(swigCPtr, this);
    return (cPtr == 0) ? null : new IRational(cPtr, false);
  }

/**
 * The time base in which all timestamps (e.g. Presentation Time Stamp 
 * (PTS)  
 * and Decompression Time Stamp (DTS)) are represented. For example 
 *  
 * if the time base is 1/1000, then the difference between a PTS of 
 * 1 and  
 * a PTS of 2 is 1 millisecond. If the timebase is 1/1, then the difference 
 *  
 * between a PTS of 1 and a PTS of 2 is 1 second.  
 * @return	The time base of this stream.  
 */
  public IRational getTimeBase() {
    long cPtr = XugglerJNI.IStream_getTimeBase(swigCPtr, this);
    return (cPtr == 0) ? null : new IRational(cPtr, false);
  }

/**
 * Return the start time, in {@link #getTimeBase()} units, when this 
 * stream  
 * started.  
 * @return	The start time.  
 */
  public long getStartTime() {
    return XugglerJNI.IStream_getStartTime(swigCPtr, this);
  }

/**
 * Return the duration, in {@link #getTimeBase()} units, of this stream, 
 *  
 * or {@link Global#NO_PTS} if unknown.  
 * @return	The duration (in getTimeBase units) of this stream, if known. 
 *		  
 */
  public long getDuration() {
    return XugglerJNI.IStream_getDuration(swigCPtr, this);
  }

/**
 * The current Decompression Time Stamp that will be used on this stream, 
 *  
 * in {@link #getTimeBase()} units.  
 * @return	The current Decompression Time Stamp that will be used on 
 *		 this stream.  
 */
  public long getCurrentDts() {
    return XugglerJNI.IStream_getCurrentDts(swigCPtr, this);
  }

/**
 * Get the number of index entries in this stream.  
 * @return	The number of index entries in this stream.  
 * @see		#getIndexEntry(int)  
 */
  public int getNumIndexEntries() {
    return XugglerJNI.IStream_getNumIndexEntries(swigCPtr, this);
  }

/**
 * Returns the number of encoded frames if known. Note that frames here 
 * means  
 * encoded frames, which can consist of many encoded audio samples, 
 * or  
 * an encoded video frame.  
 * @return	The number of frames (encoded) in this stream.  
 */
  public long getNumFrames() {
    return XugglerJNI.IStream_getNumFrames(swigCPtr, this);
  }

/**
 * Gets the sample aspect ratio.  
 * @return	The sample aspect ratio.  
 */
  public IRational getSampleAspectRatio() {
    long cPtr = XugglerJNI.IStream_getSampleAspectRatio(swigCPtr, this);
    return (cPtr == 0) ? null : new IRational(cPtr, false);
  }

/**
 * Sets the sample aspect ratio.  
 * @param	newRatio The new ratio.  
 */
  public void setSampleAspectRatio(IRational newRatio) {
    XugglerJNI.IStream_setSampleAspectRatio(swigCPtr, this, IRational.getCPtr(newRatio), newRatio);
  }

/**
 * Get the 4-character language setting for this stream.  
 * This will return null if no setting. When calling  
 * from C++, callers must ensure that the IStream outlives the  
 * value returned.  
 */
  public String getLanguage() {
    return XugglerJNI.IStream_getLanguage(swigCPtr, this);
  }

/**
 * Set the 4-character language setting for this stream.  
 * If a string longer than 4 characters is passed in, only the  
 * first 4 characters is copied.  
 * @param	language The new language setting. null is equivalent to the 
 *		  
 * empty string. strings longer than 4 characters will be truncated 
 *  
 * to first 4 characters.  
 */
  public void setLanguage(String language) {
    XugglerJNI.IStream_setLanguage(swigCPtr, this, language);
  }

/**
 * Get the underlying container for this stream, or null if Xuggler 
 *  
 * doesn't know.  
 * @return	the container, or null if we don't know.  
 */
  public IContainer getContainer() {
    long cPtr = XugglerJNI.IStream_getContainer(swigCPtr, this);
    return (cPtr == 0) ? null : new IContainer(cPtr, false);
  }

/**
 * Sets the stream coder to use for this stream.  
 * This method will only cause a change if the IStreamCoder currently 
 * set on this  
 * IStream is not open. Otherwise the call is ignore and an error is 
 * returned.  
 * @param	newCoder The new stream coder to use.  
 * @return	>= 0 on success; < 0 on error.  
 */
  public int setStreamCoder(IStreamCoder newCoder) {
    return XugglerJNI.IStream_setStreamCoder__SWIG_0(swigCPtr, this, IStreamCoder.getCPtr(newCoder), newCoder);
  }

/**
 * Get how the decoding codec should parse data from this stream.  
 * @return	the parse type.  
 * @since	3.0  
 */
  public IStream.ParseType getParseType() {
    return IStream.ParseType.swigToEnum(XugglerJNI.IStream_getParseType(swigCPtr, this));
  }

/**
 * Set the parse type the decoding codec should use. Set to  
 * {@link ParseType#PARSE_NONE} if you don't want any parsing  
 * to be done.  
 * <p>  
 * Warning: do not set this flag unless you know what you're doing, 
 *  
 * and do not set after you've started decoding.  
 * </p>  
 * @param	type The type to set.  
 * @since	3.0  
 */
  public void setParseType(IStream.ParseType type) {
    XugglerJNI.IStream_setParseType(swigCPtr, this, type.swigValue());
  }

/**
 * Get the {@link IMetaData} for this object,  
 * or null if none.  
 * <p>  
 * If the {@link IContainer} or {@link IStream} object  
 * that this {@link IMetaData} came from was opened  
 * for reading, then changes via {@link IMetaData#setValue(String, String)} 
 *  
 * will have no effect on the underlying media.  
 * </p>  
 * <p>  
 * If the {@link IContainer} or {@link IStream} object  
 * that this {@link IMetaData} came from was opened  
 * for writing, then changes via {@link IMetaData#setValue(String, String)} 
 *  
 * will have no effect after {@link IContainer#writeHeader()}  
 * is called.  
 * </p>  
 * @return	the {@link IMetaData}.  
 * @since	3.1  
 */
  public IMetaData getMetaData() {
    long cPtr = XugglerJNI.IStream_getMetaData(swigCPtr, this);
    return (cPtr == 0) ? null : new IMetaData(cPtr, false);
  }

/**
 * Set the {@link IMetaData} on this object, overriding  
 * any previous meta data. You should call this  
 * method on writable containers and  
 * before you call {@link IContainer#writeHeader}, as  
 * it probably won't do anything after that.  
 * @see		#getMetaData()  
 * @since	3.1  
 */
  public void setMetaData(IMetaData data) {
    XugglerJNI.IStream_setMetaData(swigCPtr, this, IMetaData.getCPtr(data), data);
  }

/**
 * Takes a packet destined for this stream, and stamps  
 * the stream index, and converts the time stamp to the  
 * correct units (adjusting for rounding errors between  
 * stream conversions).  
 * @param	packet to stamp  
 * @return	>= 0 on success; <0 on failure.  
 * @since	3.2  
 */
  public int stampOutputPacket(IPacket packet) {
    return XugglerJNI.IStream_stampOutputPacket(swigCPtr, this, IPacket.getCPtr(packet), packet);
  }

/**
 * Sets the stream coder to use for this stream.  
 * This method will only cause a change if the IStreamCoder currently 
 * set on this  
 * IStream is not open. Otherwise the call is ignored and an error is 
 * returned.  
 * @param	newCoder The new stream coder to use.  
 * @param	assumeOnlyStream If true then this {@link IStream} will notify 
 *		 the {@link IStreamCoder} that it is the only stream 
 *		 and the {@link IStreamCoder} may use it to determine 
 *		 time stamps to output packets with.  
 * If false then the {@link IStreamCoder}  
 * does not support automatic stamping of packets with stream index 
 * IDs  
 * and users must call {@link #stampOutputPacket(IPacket)} themselves. 
 *  
 * @return	>= 0 on success; < 0 on error.  
 * @since	3.2  
 */
  public int setStreamCoder(IStreamCoder newCoder, boolean assumeOnlyStream) {
    return XugglerJNI.IStream_setStreamCoder__SWIG_1(swigCPtr, this, IStreamCoder.getCPtr(newCoder), newCoder, assumeOnlyStream);
  }

/**
 * Search for the given time stamp in the key-frame index for this {@link 
 * IStream}.  
 * <p>  
 * Not all {@link IContainerFormat} implementations  
 * maintain key frame indexes, but if they have one,  
 * then this method searches in the {@link IStream} index  
 * to quickly find the byte-offset of the nearest key-frame to  
 * the given time stamp.  
 * </p>  
 * @param	wantedTimeStamp the time stamp wanted, in the stream's  
 * time base units.  
 * @param	flags A bitmask of the <code>SEEK_FLAG_*</code> flags, or 
 *		 0 to turn  
 * all flags off. If {@link IContainer#SEEK_FLAG_BACKWARDS} then the 
 * returned  
 * index will correspond to the time stamp which is <=  
 * the requested one (not supported by all demuxers).  
 * If {@link IContainer#SEEK_FLAG_BACKWARDS} is not set then it will 
 * be >=.  
 * if {@link IContainer#SEEK_FLAG_ANY} seek to any frame, only  
 * keyframes otherwise (not supported by all demuxers).  
 * @return	The {@link IIndexEntry} for the nearest appropriate timestamp 
 *		  
 * in the index, or null if it can't be found.  
 * @since	3.4  
 */
  public IIndexEntry findTimeStampEntryInIndex(long wantedTimeStamp, int flags) {
    long cPtr = XugglerJNI.IStream_findTimeStampEntryInIndex(swigCPtr, this, wantedTimeStamp, flags);
    return (cPtr == 0) ? null : new IIndexEntry(cPtr, false);
  }

/**
 * Search for the given time stamp in the key-frame index for this {@link 
 * IStream}.  
 * <p>  
 * Not all {@link IContainerFormat} implementations  
 * maintain key frame indexes, but if they have one,  
 * then this method searches in the {@link IStream} index  
 * to quickly find the index entry position of the nearest key-frame 
 * to  
 * the given time stamp.  
 * </p>  
 * @param	wantedTimeStamp the time stamp wanted, in the stream's  
 * time base units.  
 * @param	flags A bitmask of the <code>SEEK_FLAG_*</code> flags, or 
 *		 0 to turn  
 * all flags off. If {@link IContainer#SEEK_FLAG_BACKWARDS} then the 
 * returned  
 * index will correspond to the time stamp which is <=  
 * the requested one (not supported by all demuxers).  
 * If {@link IContainer#SEEK_FLAG_BACKWARDS} is not set then it will 
 * be >=.  
 * if {@link IContainer#SEEK_FLAG_ANY} seek to any frame, only  
 * keyframes otherwise (not supported by all demuxers).  
 * @return	The position in this {@link IStream} index, or -1 if it cannot 
 *		  
 * be found or an index is not maintained.  
 * @see		#getIndexEntry(int)  
 * @since	3.4  
 */
  public int findTimeStampPositionInIndex(long wantedTimeStamp, int flags) {
    return XugglerJNI.IStream_findTimeStampPositionInIndex(swigCPtr, this, wantedTimeStamp, flags);
  }

/**
 * Get the {@link IIndexEntry} at the given position in this  
 * {@link IStream} object's index.  
 * <p>  
 * Not all {@link IContainerFormat} types maintain  
 * {@link IStream} indexes, but if they do,  
 * this method can return those entries.  
 * </p>  
 * <p>  
 * Do not modify the {@link IContainer} this stream  
 * is from between calls to this method and  
 * {@link #getNumIndexEntries()} as indexes may  
 * be compacted while processing.  
 * </p>  
 * @param	position The position in the index table.  
 * @since	3.4  
 */
  public IIndexEntry getIndexEntry(int position) {
    long cPtr = XugglerJNI.IStream_getIndexEntry(swigCPtr, this, position);
    return (cPtr == 0) ? null : new IIndexEntry(cPtr, false);
  }

/**
 * Adds an index entry into the stream's sorted index list.  
 * Updates the entry if the list  
 * already contains it.  
 * @param	entry The entry to add.  
 * @return	>=0 on success; <0 on error.  
 * @since	3.4  
 */
  public int addIndexEntry(IIndexEntry entry) {
    return XugglerJNI.IStream_addIndexEntry(swigCPtr, this, IIndexEntry.getCPtr(entry), entry);
  }

  public enum Direction {
  /**
   * The direction this stream is going (based on the container).
   * If the container Container is opened in Container::READ mode
   * then this will be INBOUND. If it's opened in Container::WRITE
   * mode, then this will be OUTBOUND.
   */
    INBOUND,
    OUTBOUND;

    public final int swigValue() {
      return swigValue;
    }

    public static Direction swigToEnum(int swigValue) {
      Direction[] swigValues = Direction.class.getEnumConstants();
      if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
        return swigValues[swigValue];
      for (Direction swigEnum : swigValues)
        if (swigEnum.swigValue == swigValue)
          return swigEnum;
      throw new IllegalArgumentException("No enum " + Direction.class + " with value " + swigValue);
    }

    @SuppressWarnings("unused")
    private Direction() {
      this.swigValue = SwigNext.next++;
    }

    @SuppressWarnings("unused")
    private Direction(int swigValue) {
      this.swigValue = swigValue;
      SwigNext.next = swigValue+1;
    }

    @SuppressWarnings("unused")
    private Direction(Direction swigEnum) {
      this.swigValue = swigEnum.swigValue;
      SwigNext.next = this.swigValue+1;
    }

    private final int swigValue;

    private static class SwigNext {
      private static int next = 0;
    }
  }

  public enum ParseType {
  /**
   * What types of parsing can we do on a call to
   * {@link IContainer#readNextPacket(IPacket)}
   */
    PARSE_NONE,
    PARSE_FULL,
    PARSE_HEADERS,
    PARSE_TIMESTAMPS;

    public final int swigValue() {
      return swigValue;
    }

    public static ParseType swigToEnum(int swigValue) {
      ParseType[] swigValues = ParseType.class.getEnumConstants();
      if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
        return swigValues[swigValue];
      for (ParseType swigEnum : swigValues)
        if (swigEnum.swigValue == swigValue)
          return swigEnum;
      throw new IllegalArgumentException("No enum " + ParseType.class + " with value " + swigValue);
    }

    @SuppressWarnings("unused")
    private ParseType() {
      this.swigValue = SwigNext.next++;
    }

    @SuppressWarnings("unused")
    private ParseType(int swigValue) {
      this.swigValue = swigValue;
      SwigNext.next = swigValue+1;
    }

    @SuppressWarnings("unused")
    private ParseType(ParseType swigEnum) {
      this.swigValue = swigEnum.swigValue;
      SwigNext.next = this.swigValue+1;
    }

    private final int swigValue;

    private static class SwigNext {
      private static int next = 0;
    }
  }

}
