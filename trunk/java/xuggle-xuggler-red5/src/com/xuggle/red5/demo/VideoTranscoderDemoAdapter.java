/*
 * Copyright (c) 2008-2009 by Xuggle Inc. All rights reserved.
 *
 * It is REQUESTED BUT NOT REQUIRED if you use this library, that you let 
 * us know by sending e-mail to info@xuggle.com telling us briefly how you're
 * using the library and what you like or don't like about it.
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.xuggle.red5.demo;

import java.util.Timer;
import java.util.TimerTask;

import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.stream.IBroadcastStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuggle.xuggler.IContainer;

import etm.core.configuration.BasicEtmConfigurator;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.renderer.SimpleTextRenderer;
import etm.core.timer.Java15NanoTimer;

/**
 * This demo application takes a stream broadcasted from a Flash player and
 * takes each video frame and converts the left half of the picture to gray scale.
 * It does not modify the audio.
 * <p> 
 * To use install the application to a Red5 server and connect to the "videotranscoder"
 * application.
 * </p><p>
 * <b>
 * YOU MUST MAKE SURE YOU HAVE INSTALLED AAFFMPEG ON THE MACHINE RUNNING YOUR RED5 SERVER.
 * 
 * See http://aaffmpeg.theyard.net/ for instructions on how to install it.
 * </b>
 * </p><p>

 * Then publish (you can use the "demos/publisher/publisher.html" application) an audio
 * stream with a unique name (e.g. "my_stream").
 * </p><p>
 * To hear your transcoded audio stream, connect to the same application and then playback
 * a stream that has the same unique name, but with "xuggle_" appended to it (e.g. "xuggle_my_stream").
 * </p><p>
 * You should hear the audio you are broadcasting and see the video you are broadcasting,
 * just re-encoded at the new parameters.  Please
 * note that while you will hear a latency in this audio, approximately 3-msec is added by our transcoder --
 * the rest is coming from your network, from red5, and from the fact that (probably) you've set your
 * buffer time on your flash NetStream object to 2 seconds.  To see exactly how fast transcoding is,
 * check out the performance metrics that this application outputs every few seconds.
 * </p>
 */
public class VideoTranscoderDemoAdapter extends MultiThreadedApplicationAdapter
{
  private Logger log = LoggerFactory.getLogger(this.getClass());
  private VideoTranscoderDemo resamplerDemo = new VideoTranscoderDemo("xuggle_");
  private EtmMonitor profiler = EtmManager.getEtmMonitor();
  private Timer profilerRenderer = new Timer("profilerRenderer", true);
  private int mProfilerFrequency=0;

  public void setProfilerFrequency(int seconds)
  {
    mProfilerFrequency = seconds;
  }
  public void init()
  {
    log.debug("Demo has started.  Be afraid: {}", this.getClass().getName());

    // This forces us to load an AAFFMPEG shared library.  It's only
    // done because if we're debugging C++ from Java (please don't ask)
    // this allows a breakpoint
    IContainer.make();
    
    BasicEtmConfigurator.configure(true, new Java15NanoTimer());
    profiler.start();
    
    // very simple timer here that spits out profiling data every 5 seconds
    if (mProfilerFrequency > 0)
    {
      profilerRenderer.schedule(new TimerTask(){
        @Override
        public void run()
        {
          System.out.println("Printing Statistics for: " + this.getClass().getName());
          profiler.render(new SimpleTextRenderer());
        }
      },
      mProfilerFrequency*1000,
      mProfilerFrequency*1000);
    }
  }
  
  /**
   * Called on publish: NetStream.publish("streamname", "live")
   */
  @Override
  public void streamPublishStart(IBroadcastStream stream) {
    log.debug("streamPublishStart: {}; {}", stream, stream.getPublishedName());
    super.streamPublishStart(stream);
    resamplerDemo.startTranscodingStream(stream, this.getScope());
  }
  
  @Override
  public void streamBroadcastClose(IBroadcastStream stream) {
    log.debug("streamBroadcastClose: {}; {}", stream, stream.getPublishedName());
    
    resamplerDemo.stopTranscodingStream(stream, this.getScope());
    super.streamBroadcastClose(stream);
  }



}
