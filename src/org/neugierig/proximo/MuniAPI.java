// Copyright (c) 2010 Evan Martin.  All rights reserved.
// Use of this source code is governed by a BSD-style license that can
// be found in the LICENSE file.

package org.neugierig.proximo;

import java.net.*;
import java.io.*;
import org.json.*;

class MuniAPI {
  private static final String API_URL = "http://muni-api.appspot.com/api/";
  // When testing:
  // private static final String API_URL = "http://10.0.2.2:8080/api/";

  public static class QueryEntry {
    public String name;
    public String url;
    public QueryEntry(String name, String url) {
      this.name = name;
      this.url = url;
    }
    public QueryEntry() {}
    public String toString() { return this.name; }
  }

  public static class Stop extends QueryEntry {
    Stop(String name, String url) { super(name, url); }
    public static class Time {
      public Time(int minutes) {
        this.minutes = minutes;
      }
      public int minutes;
      public String toString() {
        return "" + minutes + " minutes";
      }
    }
    public Time[] times;
  }

  public static class Route extends QueryEntry {
    Route(String name, String url) { super(name, url); }
  }

  public static class Direction extends QueryEntry {
    Direction(String name, String url) { super(name, url); }
  }

  static public Route[] parseRoutes(String data) throws JSONException {
    JSONArray array = new JSONArray(data);
    Route[] routes = new Route[array.length()];
    for (int i = 0; i < array.length(); ++i) {
      JSONObject entry = array.getJSONObject(i);
      routes[i] = new Route(entry.getString("name"),
                            entry.getString("url"));
    }
    return routes;
  }

  static Direction[] parseRoute(String data) throws JSONException {
    JSONArray json = new JSONArray(data);
    Direction[] directions = new Direction[2];
    for (int i = 0; i < 2; ++i) {
      JSONObject json_dir = json.getJSONObject(i);
      directions[i] = new Direction(json_dir.getString("name"),
                                    json_dir.getString("url"));
    }
    return directions;
  }

  static Stop[] parseStops(String data) throws JSONException {
    JSONArray json = new JSONArray(data);
    Stop[] stops = new Stop[json.length()];
    for (int i = 0; i < json.length(); ++i) {
      JSONObject json_stop = json.getJSONObject(i);
      stops[i] = new Stop(json_stop.getString("name"),
                          json_stop.getString("url"));
    }
    return stops;
  }

  static Stop.Time[] parseStop(String data) throws JSONException {
    JSONArray json = new JSONArray(data);

    Stop.Time[] times = new Stop.Time[json.length()];
    for (int i = 0; i < json.length(); ++i)
      times[i] = new Stop.Time(json.getInt(i));

    return times;
  }

  static String queryNetwork(String query)
      throws MalformedURLException, IOException
  {
    return fetchURL(new URL(API_URL + query));
  }

  // It's pretty unbelievable there's no simpler way to do this.
  static String fetchURL(URL url) throws IOException {
    InputStream input = url.openStream();
    StringBuffer buffer = new StringBuffer(8 << 10);

    int byte_read;
    while ((byte_read = input.read()) != -1) {
      // This is incorrect for non-ASCII, but we don't have any of that.
      buffer.appendCodePoint(byte_read);
    }

    return buffer.toString();
  }

}
