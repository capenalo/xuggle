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
package com.xuggle.red5.io;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.IScope;
import org.red5.server.api.event.IEvent;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.IStreamCodecInfo;
import org.red5.server.api.stream.IStreamListener;
import org.red5.server.api.stream.IStreamPacket;
import org.red5.server.api.stream.IVideoStreamCodec;
import org.red5.server.api.stream.ResourceExistException;
import org.red5.server.api.stream.ResourceNotFoundException;
import org.red5.server.messaging.IMessageComponent;
import org.red5.server.messaging.IPipe;
import org.red5.server.messaging.IPipeConnectionListener;
import org.red5.server.messaging.IProvider;
import org.red5.server.messaging.OOBControlMessage;
import org.red5.server.messaging.PipeConnectionEvent;
import org.red5.server.net.rtmp.event.AudioData;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.VideoData;
import org.red5.server.stream.VideoCodecFactory;
import org.red5.server.stream.codec.StreamCodecInfo;
import org.red5.server.stream.message.RTMPMessage;

import org.slf4j.Logger;

import com.xuggle.red5.demo.AudioTranscoderDemo;

import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;

/**
 * An implementation of IBroadcastStream that allows connection-less
 * providers to still publish a Red5 stream.
 * 
 * Don't worry if you don't understand what that means.  See
 * {@link AudioTranscoderDemo} for an example of this in action.
 * 
 */
public class BroadcastStream implements IBroadcastStream, IProvider, IPipeConnectionListener
{
  /** Listeners to get notified about received packets. */
  private Set<IStreamListener> mListeners = new CopyOnWriteArraySet<IStreamListener>();
  final private Logger log = Red5LoggerFactory.getLogger(this.getClass());
  private final EtmMonitor profiler = EtmManager.getEtmMonitor();

  private String mPublishedName;
  private IPipe mLivePipe;
  private IScope mScope;

  // Codec handling stuff for frame dropping
  private StreamCodecInfo mCodecInfo;
  private VideoCodecFactory mVideoCodecFactory;
  private boolean mCreateVideoCodecInfo;


  public BroadcastStream(String name)
  {
    mPublishedName = name;
    mLivePipe = null;
    log.trace("name: {}", name);

    // we want to create a video codec when we get our
    // first video packet.
    mCreateVideoCodecInfo = false;
    mCodecInfo = new StreamCodecInfo();
    mVideoCodecFactory = null;
  }

  public IProvider getProvider()
  {
    log.trace("getProvider()");
    return this;
  }

  public String getPublishedName()
  {
    log.trace("getPublishedName()");
    return mPublishedName;
  }

  public String getSaveFilename()
  {
    log.trace("getSaveFilename()");
    throw new Error("unimplemented method");
  }

  public void addStreamListener(IStreamListener listener)
  {
    log.trace("addStreamListener(listener: {})", listener);
    mListeners.add(listener);
  }

  public Collection<IStreamListener> getStreamListeners()
  {
    log.trace("getStreamListeners()");
    return mListeners;
  }

  public void removeStreamListener(IStreamListener listener)
  {
    log.trace("removeStreamListener({})", listener);
    mListeners.remove(listener);
  }

  public void saveAs(String filePath, boolean isAppend) throws IOException,
  ResourceNotFoundException, ResourceExistException
  {
    log.trace("saveAs(filepath:{}, isAppend:{})", filePath, isAppend);
    throw new Error("unimplemented method");
  }

  public void setPublishedName(String name)
  {
    log.trace("setPublishedName(name:{})", name);
    mPublishedName = name;
  }

  public void close()
  {
    log.trace("close()");
  }

  public IStreamCodecInfo getCodecInfo()
  {
    log.trace("getCodecInfo()");
    // we don't support this right now.
    return mCodecInfo;
  }

  public String getName()
  {
    log.trace("getName(): {}", mPublishedName);
    // for now, just return the published name
    return mPublishedName;
  }

  public void setScope(IScope scope)
  {
    mScope = scope;
  }

  public IScope getScope()
  {
    log.trace("getScope(): {}", mScope);
    return mScope;
  }

  public void start()
  {
    log.trace("start()");
    // Red5 uses a nice handy dandy bean context for this but we're not.
    try {
      mVideoCodecFactory = (VideoCodecFactory) getScope().getContext()
      .getBean(VideoCodecFactory.KEY);
      mCreateVideoCodecInfo = true;
    } catch (Exception err) {
      log.warn("No video codec factory available.", err);
    }

  }

  public void stop()
  {
    log.trace("stop");
  }

  public void onOOBControlMessage(IMessageComponent source, IPipe pipe,
      OOBControlMessage oobCtrlMsg)
  {
    log.trace("onOOBControlMessage");
  }

  public void onPipeConnectionEvent(PipeConnectionEvent event)
  {
    log.trace("onPipeConnectionEvent(event:{})", event);
    switch (event.getType())
    {
    case PipeConnectionEvent.PROVIDER_CONNECT_PUSH:
      if (event.getProvider() == this
          && (event.getParamMap() == null || !event.getParamMap()
              .containsKey("record")))
      {
        this.mLivePipe = (IPipe) event.getSource();
      }
      break;
    case PipeConnectionEvent.PROVIDER_DISCONNECT:
      if (this.mLivePipe == event.getSource())
      {
        this.mLivePipe = null;
      }
      break;
    case PipeConnectionEvent.CONSUMER_CONNECT_PUSH:
      break;
    case PipeConnectionEvent.CONSUMER_DISCONNECT:
      break;
    default:
      break;
    }
  }

  public void dispatchEvent(IEvent event)
  {
    EtmPoint point = profiler.createPoint(this.getClass().getName()+"#dispatchEvent");
    try {
      log.trace("dispatchEvent(event:{})", event);
      if (event instanceof IRTMPEvent)
      {
        IRTMPEvent rtmpEvent = (IRTMPEvent) event;
        if (mLivePipe != null)
        {
          RTMPMessage msg = new RTMPMessage();

          msg.setBody(rtmpEvent);
          try
          {
            if (event instanceof AudioData)
            {
              mCodecInfo.setHasAudio(true);
            }
            else if (event instanceof VideoData)
            {
              IVideoStreamCodec videoStreamCodec = null;
              if (mVideoCodecFactory != null && mCreateVideoCodecInfo)
              {
                videoStreamCodec = mVideoCodecFactory.getVideoCodec(((VideoData) event).getData());
                mCodecInfo.setVideoCodec(videoStreamCodec);
                mCreateVideoCodecInfo= false;
              } else if (mCodecInfo != null) {
                videoStreamCodec = mCodecInfo.getVideoCodec();
              }

              if (videoStreamCodec != null) {
                videoStreamCodec.addData(((VideoData) rtmpEvent).getData());
              }

              if (mCodecInfo!= null) {
                mCodecInfo.setHasVideo(true);
              }

            }
            mLivePipe.pushMessage(msg);

            // Notify listeners about received packet
            if (rtmpEvent instanceof IStreamPacket)
            {
              for (IStreamListener listener : getStreamListeners())
              {
                try
                {
                  listener.packetReceived(this, (IStreamPacket) rtmpEvent);
                }
                catch (Exception e)
                {
                  log.error("Error while notifying listener " + listener, e);
                }
              }
            }

          }
          catch (IOException ex)
          {
            // ignore
            log.error("Got exception: {}", ex);
          }
        }
      }
    } finally {
      point.collect();
    }
  }
}
