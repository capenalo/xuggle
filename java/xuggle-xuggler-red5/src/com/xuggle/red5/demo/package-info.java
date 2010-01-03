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

