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

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuggle.red5.io.IRTMPEventIOHandler;
import com.xuggle.red5.io.Red5HandlerFactory;
import com.xuggle.red5.io.Red5Message;
import com.xuggle.test_utils.NameAwareTestClassRunner;

import com.xuggle.xuggler.ISimpleMediaFile;
import com.xuggle.xuggler.SimpleMediaFile;
import com.xuggle.xuggler.io.FfmpegIOHandle;
import com.xuggle.xuggler.io.Helper;
import com.xuggle.xuggler.io.IURLProtocolHandler;
import com.xuggle.xuggler.io.IURLProtocolHandlerFactory;
import com.xuggle.xuggler.io.URLProtocolManager;

@RunWith(NameAwareTestClassRunner.class)
public class Red5HandlerFactoryTest
{
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private String mTestName = null;

  static private final String mRed5URLProtocol = "redfivevl";

  @Before
  public void setUp()
  {
    mTestName = NameAwareTestClassRunner.getTestMethodName();
    log.debug("Running test: {}", mTestName);
  }

  @Test
  public void testAddingAndDeletingFactory()
  {
    URLProtocolManager manager;
    Red5HandlerFactory factory;
    IURLProtocolHandlerFactory oldFactory;

    manager = URLProtocolManager.getManager();
    assertTrue("could not find the protocol manager", manager != null);

    factory = Red5HandlerFactory.getFactory(mRed5URLProtocol);

    // register but make sure the old one is returned.
    oldFactory = manager.registerFactory(mRed5URLProtocol, factory);
    assertTrue("did not return old factory", oldFactory == factory);

    // register a null handler
    oldFactory = manager.registerFactory(mRed5URLProtocol, null);
    assertTrue("did not find old handler", oldFactory != null);

    // Make sure that getting a file with this protocol fails,
    // but fails without an error.
    String url = mRed5URLProtocol + ":nonExistentProtocol";
    int retval = -1;
    FfmpegIOHandle ffmpegHandle = new FfmpegIOHandle();
    retval = Helper.url_open(ffmpegHandle, url,
        IURLProtocolHandler.URL_RDONLY_MODE);
    assertTrue("Ffmpeg found the stream??: " + url, retval == -1);
  }

  @Test
  public void testFindProtocolHandler()
  {
    IRTMPEventIOHandler ioHandler = new IRTMPEventIOHandler() {
      public Red5Message read()
      {
        return null;
      }
      public void write(Red5Message msg)
      {
      }
    };
    IRTMPEventIOHandler oldHandler = null;
    IURLProtocolHandler handler = null;
    int retval = -1;
    URLProtocolManager manager = null;
    ;
    Red5HandlerFactory factory = null;
    IURLProtocolHandlerFactory oldFactory = null;
    FfmpegIOHandle ffmpegHandle = new FfmpegIOHandle();

    manager = URLProtocolManager.getManager();
    assertTrue("could not find the protocol manager", manager != null);

    factory = new Red5HandlerFactory();
    oldFactory = manager.registerFactory(mRed5URLProtocol, factory);
    assertTrue("no previously registered factory", oldFactory == null);

    ISimpleMediaFile file = new SimpleMediaFile();
    file.setURL(mRed5URLProtocol+":input");
    oldHandler = factory.registerStream(ioHandler, file);
    assertTrue("a buffer already registered", oldHandler == null);

    String goodUrl = mRed5URLProtocol + ":" + "input";
    String badUrl = mRed5URLProtocol + ":" + "shouldNotExist";

    handler = factory.getHandler(mRed5URLProtocol, goodUrl,
        IURLProtocolHandler.URL_RDONLY_MODE);
    assertTrue("Did not find handler for registered stream",
        handler != null);

    handler = factory.getHandler(mRed5URLProtocol, badUrl,
        IURLProtocolHandler.URL_RDONLY_MODE);
    assertTrue("Found unexpected handler for stream", handler == null);

    // Now, test that we can successfully trick Ffmpeg into opening the
    // file.
    retval = Helper.url_open(ffmpegHandle, goodUrl,
        IURLProtocolHandler.URL_RDONLY_MODE);
    assertTrue("Ffmpeg could not find the stream: " + goodUrl, retval >= 0);

    retval = Helper.url_open(ffmpegHandle, badUrl,
        IURLProtocolHandler.URL_RDONLY_MODE);
    assertTrue("Ffmpeg found the unexpected stream: " + badUrl,
        retval == -1);

    // and make sure we can't open the bad URL.

    // now delete the stream
    oldHandler = factory.deleteStream("input");
    assertTrue("did not get a buffer back when deleting a stream",
        oldHandler != null);
    assertTrue("did not get readbuffer back", oldHandler == ioHandler);

    handler = factory.getHandler(mRed5URLProtocol, goodUrl,
        IURLProtocolHandler.URL_RDONLY_MODE);
    assertTrue("found unexpected handler for delete stream",
        handler == null);
  }
}
