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

package com.xuggle.utils.net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.xuggle.utils.collections.IKeyValuePair;
import com.xuggle.utils.collections.KeyValuePair;
import com.xuggle.utils.collections.MapUtils;
import com.xuggle.utils.collections.MapUtils.ListToMapMode;

/**
 * Methods for working with YouTube urls.
 */
public class YouTube
{
  /**
   * Gets all parameters we can figure out about this youTubeId by scraping YouTube.
   * The URL for the underlying video will have the key "location".
   * @param youTubeId A YouTube ID
   * @return A map with all parameters as key-value pairs.
   * @throws IOException if we have networking troubles
   */
  public static Map<String, String> getVideoInfo(String youTubeId) throws IOException
  {
    final Map<String, String> retval = new HashMap<String, String>();
    getVideoInfo(youTubeId, retval);
    return retval;
  }
  /**
   * Gets all parameters we can figure out about this youTubeId by scraping YouTube.
   * The URL for the underlying video will have the key "location".
   * @param youTubeId A YouTube ID
   * @param map A map to fill with data; it is not cleared first.
   * @throws IOException if we have networking troubles
   */
  public static void getVideoInfo(String youTubeId, Map<String, String> map) throws IOException
  {
    final String host = "http://www.youtube.com";
    final List<IKeyValuePair> params = new ArrayList<IKeyValuePair>();
    params.add(new KeyValuePair("video_id", youTubeId));
    final String urlString = host + "/get_video_info?&"+URLParams.generateQueryString(params);
    
    final URL url;
    try
    {
      url = new URL(urlString);
    }
    catch (MalformedURLException e)
    {
      throw new RuntimeException("malformed url: " + urlString, e);
    }
    HttpURLConnection conn;
    BufferedInputStream in;
    byte[] data = new byte[4096]; // bad Art; fixed size
    
    conn = (HttpURLConnection)url.openConnection();
    if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
      // badness.
      conn.disconnect();
      throw new RuntimeException("could not get video info: " + urlString);
    }
    in = new BufferedInputStream(conn.getInputStream());      
    in.read(data);
    // convert to string; ugh
    String response = new String(data, "UTF-8");
    // convert into parameter map
    final Map<String, String> youTubeParams = MapUtils.listToMap(URLParams.parseQueryString(response), ListToMapMode.FIRST_WINS);
    final String token = youTubeParams.get("token");
    if (token == null)
      throw new RuntimeException("Could not find youtube token: "+ urlString +"; response="+response);
    
    params.clear();
    params.add(new KeyValuePair("video_id", youTubeId));
    params.add(new KeyValuePair("t", token));
    final String location = host + "/get_video?"+URLParams.generateQueryString(params);
    map.put("location", location);
    map.putAll(youTubeParams);
  }
  
  /**
   * For a given youTubeId, returns a URL that can be used to retrieve the FLV
   * behind it.  This URL may not work, and if it does, may expire at any later time.
   * 
   * @param youTubeId A youtube video identifier, e.g. "nQm-O6jiNsY"
   * @return The URL
   */
  public static String getLocation(String youTubeId)
  {
    try
    {
      return getVideoInfo(youTubeId).get("location");
    }
    catch (IOException e)
    {
      throw new RuntimeException("could not get location from youtube", e);
    }
  }
  public static void main(String[] args)
  {
    try
    {
      final String youTubeId = "nQm-O6jiNsY";
      final Map<String, String> params = getVideoInfo(youTubeId);
      final Set<Entry<String, String>> entries = params.entrySet();
      System.out.println("YouTube ID: "+youTubeId);
      for(Entry<String, String> entry: entries)
      {
        System.out.println(entry.getKey()+" = "+entry.getValue());
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
}
