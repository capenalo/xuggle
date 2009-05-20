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
import com.xuggle.xuggler.ISimpleMediaFile;
import com.xuggle.xuggler.SimpleMediaFile;

import com.xuggle.red5.Transcoder;
import com.xuggle.red5.io.BroadcastStream;

/**
 * This demo application takes a stream broadcasted from a Flash player and
 * decodes, re-samples, and re-encodes the audio on that stream to produce
 * 5.5KHZ stero PCM audio.  It drops all the video.
 * <p> 
 * To use install the application to a Red5 server and connect to the "audiotranscoder"
 * application.
 * </p><p>
 * <b>
 * YOU MUST MAKE SURE YOU HAVE INSTALLED XUGGLER ON THE MACHINE RUNNING YOUR RED5 SERVER.
 * 
 * See http://www.xuggle.com/xuggler
 * </b>
 * </p><p>

 * Then publish (you can use the "demos/publisher/publisher.html" application) an audio
 * stream with a unique name (e.g. "my_stream").
 * </p><p>
 * To hear your transcoded audio stream, connect to the same application and then playback
 * a stream that has the same unique name, but with "xuggle_" appended to it (e.g. "xuggle_my_stream").
 * </p><p>
 * You should hear the audio you are broadcasting, just re-encoded at the new parameters.  Please
 * note that while you will hear a latency in this audio, approximately 3-msec is added by our transcoder --
 * the rest is coming from your network, from red5, and from the fact that (probably) you've set your
 * buffer time on your flash NetStream object to 2 seconds.  To see exactly how fast transcoding is,
 * check out the performance metrics that this application outputs every few seconds.
 * </p>
 */
public class AudioTranscoderDemo
{
  final private Logger log = Red5LoggerFactory.getLogger(this.getClass());
  final private String mStreamPrefix;
  final private Map<String, BroadcastStream> mOutputStreams = new HashMap<String, BroadcastStream>();
  final private Map<String, Transcoder> mTranscoders = new HashMap<String, Transcoder>();

  /**
   * Create a new resampler demo object.
   * @param streamPrefix The prefix to attach to the name of the stream copies we make.
   */
  public AudioTranscoderDemo(String streamPrefix)
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
     * First, we create the meta data information about our stream.
     * 
     * Turns out aaffmpeg-red5 provides an object to do that with.
     */
    ISimpleMediaFile inputStreamInfo = new SimpleMediaFile();
    
    // now we're going to default to no video, and lots of audio,
    // but we're going to ask AAFFMPEG to figure out as much
    // data as possible.  This means AAFFMPEG will have to look at at
    // least ONE audio packet before finishing the IContainer open call.
    inputStreamInfo.setHasVideo(false);
    inputStreamInfo.setHasAudio(true);
    
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
    outputStreamInfo.setAudioSampleRate(22050/4);
    outputStreamInfo.setAudioChannels(2);
    outputStreamInfo.setAudioBitRate(32000);
    outputStreamInfo.setAudioCodec(ICodec.ID.CODEC_ID_PCM_S16LE);
    outputStreamInfo.setHasVideo(false);
    
    /**
     * And finally, let's create out transcoder
     */
    Transcoder transcoder = new Transcoder(aStream,
        outputStream, outputStreamInfo);
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
