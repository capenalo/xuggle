/*
 * This file is part of Xuggler.
 * 
 * Xuggler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Xuggler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Xuggler.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xuggle.red5.demo;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.IContext;
import org.red5.server.api.IScope;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.stream.BroadcastScope;
import org.red5.server.stream.IBroadcastScope;
import org.red5.server.stream.IProviderService;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.ISimpleMediaFile;
import com.xuggle.xuggler.SimpleMediaFile;

import com.xuggle.ferry.IBuffer;
import com.xuggle.red5.VideoPictureListener;
import com.xuggle.red5.IVideoPictureListener;
import com.xuggle.red5.Transcoder;
import com.xuggle.red5.io.BroadcastStream;

/**
 * This class takes an IBroadcastStream and modifies each video picture
 * to mirror the right half in the left half, and grayscale the left half.
 */
public class VideoTranscoderDemo
{
  final private Logger log = Red5LoggerFactory.getLogger(this.getClass());
  final private String mStreamPrefix;
  final private Map<String, BroadcastStream> mOutputStreams = new HashMap<String, BroadcastStream>();
  final private Map<String, Transcoder> mTranscoders = new HashMap<String, Transcoder>();
  final static byte[] mGrayScaleMask = new byte[2048]; // fail if lower that this.
  static {
    // sets the values to set the U and V parts of an image to in order to gray them out.
    for(int i = 0; i < mGrayScaleMask.length; i++)
      mGrayScaleMask[i]=(byte)127;
  }
  
  /**
   * Takes each video picture just prior to encoding, and mirrors the right half over the left half,
   * and grayscales the resulting left half.
   * <p>
   * This Video Picture Listener is called every time the {@link Transcoder} is about to
   * encode a video picture.  It allows us to replace or edit the picture it is encoding.  The
   * {@link Transcoder} blocks while this function executes, so try to be fast.
   * </p><p>
   * Don't worry if you don't understand all the image math here; the important
   * thing on first reading is to see that it is possible to modify images
   * mid-broadcast.  Take our word that it works.
   * </p>
   */
  final private IVideoPictureListener mVideoPictureListener = new VideoPictureListener()
  {
    /**
     * Takes the input picture, and if in YUV420P pixel format, converts it to
     * half grayscale
     */
    public IVideoPicture preEncode(IVideoPicture picture)
    {
      final boolean doGrayscale = true;
      if (doGrayscale && picture.isComplete() && picture.getPixelType() == IPixelFormat.Type.YUV420P)
      {
        log.debug("grayscaling half of a video picture for fun and profit");
        int width = picture.getWidth();
        int height = picture.getHeight();
        
        /**
         * For illustration purposes we are going to modify the IVideoPicture data directly.
         * 
         * Warning for those reading the code:  The java.nio.ByteBuffer instance below
         * is only valid as long as the IBuffer is not collected by the JVM.  Unfortunately
         * if the IBuffer is collected, future references to the ByteBuffer may crash the JVM.
         * 
         * As a general rule of thumb, only ever pass around references to IBuffer, and locally
         * get the ByteBuffer only when needed (getting a ByteBuffer is FAST).
         */
        IBuffer buffer = picture.getData();
        java.nio.ByteBuffer bytes = buffer.getByteBuffer(0, buffer.getBufferSize());
        
        // First we mirror just the Y pixels from the right half to the left half.
        int pixelWidthToMirror = width /2; // round down
        
        // Iterate line by line through the Y plane
        for(int line=0; line < height; line++)
        {
          int lineStart = line*width; // find the offset in the buffer where the next Y line starts
          for(int i = 0; i < pixelWidthToMirror; i++)
          {
            // this line reverses the bits in each Y line
            bytes.put(lineStart+i, bytes.get(lineStart+width-1-i));
          }
        }
        
        // YUV420 P contains all the Y pixels, then all the U values and then all the V values in a picture.
        // To make gray scale, simply leave all Y pixels and 0 out all the U and V values in the line
        // that are beyond half the picture.
        int uwidth = -((-width)>>1); // round-up
        int uheight = -((-height)>>1); // round-up

        int pixelWidthToGray = Math.min(uwidth/2, mGrayScaleMask.length); // round down
        if (pixelWidthToGray > 0)
        {
          int startingUOffset = width*height; // skip all Y values
          int startingVOffset = width*height + uwidth*uheight; // skip all Y and all U values
          for(int line = 0; line < uheight; line++)
          {
            // Find the U values for the line for we're processing
            bytes.position(startingUOffset+line*uwidth);
            // And gray out the first half of the line
            bytes.put(mGrayScaleMask, 0, pixelWidthToGray);
            // Find the V values for the line we're processing
            bytes.position(startingVOffset+line*uwidth);
            // And gray out the first half of the line
            bytes.put(mGrayScaleMask, 0, pixelWidthToGray);
          }
        }
        bytes = null; // tell the JVM they can collect this when they want.
        // and release the IBuffer
        buffer = null;
      }
      return picture;
    }

  };

  /**
   * Create a new resampler demo object.
   * @param streamPrefix The prefix to attach to the name of the stream copies we make.
   */
  public VideoTranscoderDemo(String streamPrefix)
  {
    mStreamPrefix = streamPrefix;
  }

  /**
   * @return the streamPrefix
   */
  public String getStreamPrefix()
  {
    return mStreamPrefix;
  }

  /**
   * Starts transcoding this stream.  This method is a no-op if
   * this stream is already a stream copy created by this
   * transcoder.
   * @param aStream The stream to copy.
   * @param aScope The application scope.
   */
  synchronized public void startTranscodingStream(IBroadcastStream aStream, IScope aScope)
  {
    log.debug("startTranscodingStream({},{})", aStream.getPublishedName(), aScope.getName());
    if (aStream.getPublishedName().startsWith(getStreamPrefix()))
    {
      log.debug("Not making a copy of a copy: {}", aStream.getPublishedName());
      return;
    }
    log.debug("Making transcoded version of: {}", aStream.getPublishedName());

    /*
     * Now, we need to set up the output stream we want to broadcast to.
     * Turns out aaffmpeg-red5 provides one of those.
     */
    String outputName = getStreamPrefix()+aStream.getPublishedName();
    BroadcastStream outputStream = new BroadcastStream(outputName);
    outputStream.setPublishedName(outputName);
    outputStream.setScope(aScope);

    IContext context = aScope.getContext();

    IProviderService providerService = (IProviderService) context
    .getBean(IProviderService.BEAN_NAME);
    if (providerService.registerBroadcastStream(aScope, outputName,
        outputStream))
    {
      IBroadcastScope bsScope = (BroadcastScope) providerService
      .getLiveProviderInput(aScope, outputName, true);

      bsScope.setAttribute(IBroadcastScope.STREAM_ATTRIBUTE, outputStream);
    }
    else
    {
      log.error("Got a fatal error; could not register broadcast stream");
      throw new RuntimeException("fooey!");
    }
    mOutputStreams.put(aStream.getPublishedName(), outputStream);
    outputStream.start();
    
    /**
     * Now let's give aaffmpeg-red5 some information about what we want to transcode as. 
     */
    ISimpleMediaFile outputStreamInfo = new SimpleMediaFile();
    outputStreamInfo.setHasAudio(true);
    outputStreamInfo.setAudioBitRate(32000);
    outputStreamInfo.setAudioChannels(1);
    outputStreamInfo.setAudioSampleRate(22050);
    outputStreamInfo.setAudioCodec(ICodec.ID.CODEC_ID_MP3);
    outputStreamInfo.setHasVideo(true);
    // Unfortunately the Trans-coder needs to know the width and height
    // you want to output as; even if you don't know yet.
    outputStreamInfo.setVideoWidth(320);
    outputStreamInfo.setVideoHeight(240);
    outputStreamInfo.setVideoBitRate(320000);
    outputStreamInfo.setVideoCodec(ICodec.ID.CODEC_ID_FLV1);
    outputStreamInfo.setVideoGlobalQuality(0);
    
    /**
     * And finally, let's create out transcoder
     */
    Transcoder transcoder = new Transcoder(aStream,
        outputStream, outputStreamInfo,
        null, null, mVideoPictureListener);
    Thread transcoderThread = new Thread(transcoder);
    transcoderThread.setDaemon(true);
    mTranscoders.put(aStream.getPublishedName(), transcoder);
    log.debug("Starting transcoding thread for: {}", aStream.getPublishedName());
    transcoderThread.start();
  }

  /**
   * Stop transcoding a stream.
   * @param aStream The stream to stop transcoding.
   * @param aScope The application scope.
   */
  synchronized public void stopTranscodingStream(IBroadcastStream aStream, IScope aScope)
  {
    log.debug("stopTranscodingStream({},{})", aStream.getPublishedName(), aScope.getName());
    String inputName = aStream.getPublishedName(); 
    Transcoder transcoder = mTranscoders.get(inputName);
    if (transcoder != null)
    {
      transcoder.stop();
    }
    BroadcastStream outputStream = mOutputStreams.get(inputName);
    if (outputStream != null)
    {
      outputStream.stop();
    }
  }
  
}
