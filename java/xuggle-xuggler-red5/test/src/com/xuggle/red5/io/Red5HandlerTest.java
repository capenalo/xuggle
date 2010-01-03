/*******************************************************************************
 * Copyright (c) 2008, 2010 Xuggle Inc.  All rights reserved.
 *  
 * This file is part of Xuggle-Xuggler-Red5.
 *
 * Xuggle-Xuggler-Red5 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Xuggle-Xuggler-Red5 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Xuggle-Xuggler-Red5.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package com.xuggle.red5.io;

import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuggle.red5.io.Red5HandlerFactory;
import com.xuggle.red5.io.Red5Message;
import com.xuggle.red5.io.Red5StreamingQueue;
import com.xuggle.test_utils.NameAwareTestClassRunner;

import com.xuggle.xuggler.ISimpleMediaFile;
import com.xuggle.xuggler.SimpleMediaFile;
import com.xuggle.xuggler.io.FfmpegIOHandle;
import com.xuggle.xuggler.io.Helper;
import com.xuggle.xuggler.io.IURLProtocolHandler;
import com.xuggle.xuggler.io.URLProtocolManager;

import static junit.framework.Assert.*;

@RunWith(NameAwareTestClassRunner.class)
public class Red5HandlerTest
{
  private String mTestName = null;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  static private final String mRed5URLProtocol = "redfivevl";
  static private final String mSampleFile = "fixtures/testfile.flv";

  static private Throwable sThreadError = null;

  URLProtocolManager mManager;
  Red5HandlerFactory mFactory;

  @Before
  public void setUp()
  {
    mTestName = NameAwareTestClassRunner.getTestMethodName();
    mManager = URLProtocolManager.getManager();

    mFactory = Red5HandlerFactory.getFactory(mRed5URLProtocol);
  }

  @Test(timeout=30000)
  public void testWriteToAVBufferStream()
  {
    final String streamName = "testStream";
    final String ffmpegStreamName = mRed5URLProtocol + ":" + streamName;

    Red5StreamingQueue buffer = new Red5StreamingQueue();
    
    int retval = -1;

    // register our stream with our red5 protocol factory
    ISimpleMediaFile file = new SimpleMediaFile();
    file.setURL(ffmpegStreamName);
    mFactory.registerStream(buffer, file);

    // start up a thread to consume the other end of the AVBufferStream
    Runnable consumer = new BufferConsumer(buffer);

    retval = readAndWriteStreams("file:" + mSampleFile, ffmpegStreamName,
        consumer, 10000);
    assertTrue("looks like test failed...", retval == 0);
  }

  /*
   * Tests reading from an AVBuffer stream
   * 
   * The AVBufferStream is actually created using the AVBufferStream
   * writer method, so if the write tests fails, the read test
   * fails too.
   */
  @Test(timeout=30000)
  public void testReadFromAVBufferStream()
  {
    final String streamName = "testStream";
    final String ffmpegStreamName = mRed5URLProtocol + ":" + streamName;

    Red5StreamingQueue buffer = new Red5StreamingQueue();
    
    int retval = -1;

    // register our stream with our red5 protocol factory
    ISimpleMediaFile file = new SimpleMediaFile();
    file.setURL(ffmpegStreamName);
    mFactory.registerStream(buffer, file);

    // start up a thread to produce the required buffer stream messages.
    Runnable producer = new BufferProducer("file:" + mSampleFile,
        ffmpegStreamName);

    retval = readAndWriteStreams(ffmpegStreamName, this.getClass().getName()+"_" +mTestName
        + ".flv", producer, 10000);
    assertTrue("looks like test failed...", retval == 0);

  }

  // just eats a buffer until an END_STREAM comes along.
  private class BufferConsumer implements Runnable
  {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Red5StreamingQueue mBuffer;

    public BufferConsumer(Red5StreamingQueue buffer)
    {
      mBuffer = buffer;
    }

    public void run()
    {
      log.debug("starting thread: {}", Thread.currentThread().getName());
      Red5Message msg = null;
      do
      {
        try
        {
          msg = mBuffer.take();
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
          throw new RuntimeException("Got interrupted");
        }
      }
      while (msg.getType() != Red5Message.Type.END_STREAM);
      log.debug("exiting thread: {}", Thread.currentThread().getName());
    }
  }

  private class BufferProducer implements Runnable
  {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String mInputStreamName;
    private String mOutputStreamName;

    public BufferProducer(String inputStreamName, String outputStreamName)
    {
      mInputStreamName = inputStreamName;
      mOutputStreamName = outputStreamName;
    }

    public void run()
    {
      log.debug("starting thread: {}", Thread.currentThread().getName());
      readAndWriteStreams(mInputStreamName, mOutputStreamName, null, 0);
      log.debug("exiting thread: {}", Thread.currentThread().getName());
    }
  }

  private int readAndWriteStreams(String inputStreamName,
      String outputStreamName, Runnable red5BufferProcessor,
      long threadTimeout)
  {
    FfmpegIOHandle readHandle = new FfmpegIOHandle();
    FfmpegIOHandle writeHandle = new FfmpegIOHandle();

    int retval = -1;
    long bytesCopied = -1;

    retval = Helper.url_open(readHandle, inputStreamName,
        IURLProtocolHandler.URL_RDONLY_MODE);
    assertTrue("could not open input stream: " + inputStreamName,
        retval >= 0);

    // Now, open our output file.
    retval = Helper.url_open(writeHandle, outputStreamName,
        IURLProtocolHandler.URL_WRONLY_MODE);
    assertTrue("could not open output stream: " + outputStreamName,
        retval >= 0);

    Thread processorThread = startProcessorThread(red5BufferProcessor);

    // now loop through the buffer stream.
    bytesCopied = copyStreams(readHandle, writeHandle);
    log.debug("copied {} total bytes from \"{}\" to \"{}\"", new Object[]
    {
        bytesCopied, inputStreamName, outputStreamName
    });

    assertTrue("did not copy streams correctly", bytesCopied > 0);

    // close our two handles.
    retval = Helper.url_close(readHandle);
    assertTrue("closed ffmpeg handle", retval >= 0);

    retval = Helper.url_close(writeHandle);
    assertTrue("closed red5 handle", retval >= 0);

    waitForProcessorThread(processorThread, threadTimeout);

    return retval;
  }

  private Thread startProcessorThread(Runnable processor)
  {
    Thread newThread = null;

    if (processor != null)
    {
      newThread = new Thread(processor, "Processor");
      newThread
          .setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
          {
            public void uncaughtException(Thread t, Throwable e)
            {
              sThreadError = e;
              e.printStackTrace();
            }
          });
      newThread.start();
    }
    return newThread;
  }

  private void waitForProcessorThread(Thread processorThread,
      long threadTimeout)
  {
    if (processorThread != null)
    {
      Thread.State state;
      state = processorThread.getState();
      log.debug("waiting for thread; before join: {}", state);
      try
      {
        processorThread.join(threadTimeout);
      }
      catch (InterruptedException ex)
      {
        log.debug("unexpected interrupted: {}", ex);
      }
      state = processorThread.getState();
      log.debug("waiting for thread; after join: {}", state);
      if (state != Thread.State.TERMINATED)
      {
        Thread.yield();
        fail("processor thread did not finish on time");
      }
      if (sThreadError != null)
      {
        fail("processor thread failed with error: " + sThreadError);
      }
    }
  }

  private static long copyStreams(FfmpegIOHandle readHandle,
      FfmpegIOHandle writeHandle)
  {
    int retval = -1;
    // now loop through the buffer stream.
    byte[] buf = new byte[1024];
    long bytesRead = 0;
    long bytesWritten = 0;
    boolean firstTime = true;
    do
    {
      retval = Helper.url_read(readHandle, buf, buf.length);
      if (firstTime)
      {
        assertTrue("did not read any bytes for readHandle", retval > 0);
        firstTime = false;

        // make sure this is an FLV
        Byte[] fileMarker =
        {
            'F', 'L', 'V'
        };
        for (int i = 0; i < fileMarker.length; i++)
        {
          assertTrue("incorrect header on readHandle",
              buf[i] == fileMarker[i]);
        }

      }
      if (retval > 0)
      {
        bytesRead += retval;

        int bytesToWrite = retval;
        retval = Helper.url_write(writeHandle, buf,
            bytesToWrite);

        if (retval > 0)
          bytesWritten += retval;
        assertTrue("did not write bytes successfully on retval",
            retval == bytesToWrite);
      }
    }
    while (retval > 0);

    return bytesWritten;
  }

}
