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

/**
 * Demonstration Red5 applications that use the {@link com.xuggle.red5.Transcoder} object.
 *
 * <h1>audiotranscoder</h1>
 * This demonstration intercepts any red5 stream that is published to it (e.g. "foo"),
 * strips out all the video, decodes the
 * audio, downsamples it to 5.5 khz stereo audio, and broadcasts a new stream with "copy_" prepending
 * the original stream (e.g. "copy_foo"), but this time sending out raw PCM data.
 * 
 * <h1>videotranscoder</h1>
 * This demonstration intercepts any red5 stream that is published to it (e.g. "foo"),
 * converts the audio data to 22khz mp3 audio, and decodes each video frame, mirrors
 * the right half of each video frame in the left half, and grayscales the left half.  
 */
package com.xuggle.red5.demo;

