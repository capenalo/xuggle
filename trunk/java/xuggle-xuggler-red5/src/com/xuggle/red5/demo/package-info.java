/*
 * Copyright (c) 2008, 2009 by Xuggle Incorporated.  All rights reserved.
 * 
 * This file is part of Xuggler.
 * 
 * You can redistribute Xuggler and/or modify it under the terms of the GNU
 * Affero General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Xuggler is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with Xuggler.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Demonstration Red5 applications that use the {@link com.xuggle.red5.Transcoder} object.
 *
 * <h1>audiotranscoder</h1>
 * This demonstration intercepts any red5 stream that is published to it (e.g. "foo"),
 * strips out all the video, decodes the
 * audio, downsamples it to 5.5 khz stereo audio, and broadcasts a new stream with "xuggle_" prepending
 * the original stream (e.g. "xuggle_"), but this time sending out raw PCM data.
 * 
 * <h1>videotranscoder</h1>
 * This demonstration intercepts any red5 stream that is published to it (e.g. "foo"),
 * converts the audio data to 22khz mp3 audio, and decodes each video frame, mirrors
 * the right half of each video frame in the left half, and grayscales the left half.  
 */
package com.xuggle.red5.demo;

