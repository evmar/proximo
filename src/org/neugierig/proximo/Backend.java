// Copyright (c) 2010 Evan Martin.  All rights reserved.
// Use of this source code is governed by a BSD-style license that can
// be found in the LICENSE file.

package org.neugierig.proximo;

import android.content.*;
import android.os.*;
import android.util.Log;

import java.net.*;
import java.io.*;
import org.json.*;

// The Backend knows how to query both the local database and the
// remote server for MUNI data.  Its calls are all blocking and so
// it's typically used from a background thread.
public class Backend {
  // Tag for logging.
  private static final String TAG = "proximo";
  // Intent extra indicating the backend query.
  public static final String KEY_QUERY = "query";

  Backend(Context context) {
    mContext = context;
    mDatabase = new Database(context);
  }

  MuniAPI.Route[] fetchRoutes()
      throws MalformedURLException, IOException, JSONException
  {
    return MuniAPI.parseRoutes(queryAPI("", false));
  }

  MuniAPI.Direction[] fetchRoute(String query)
      throws MalformedURLException, IOException, JSONException
  {
    return MuniAPI.parseRoute(queryAPI(query, false));
  }

  MuniAPI.Stop[] fetchStops(String query)
      throws MalformedURLException, IOException, JSONException
  {
    return MuniAPI.parseStops(queryAPI(query, false));
  }

  MuniAPI.Stop.Time[] fetchStop(String query, boolean force_refresh)
      throws MalformedURLException, IOException, JSONException
  {
    return MuniAPI.parseStop(queryAPI(query, force_refresh));
  }

  String queryAPI(String query, boolean reload)
      throws MalformedURLException, IOException
  {
    String data = null;

    if (!reload)
      data = mDatabase.get(query);

    if (data == null) {
      data = MuniAPI.queryNetwork(query);
      Log.i(TAG, "Network fetch: " + data);
      mDatabase.put(query, data);
    }

    return data;
  }

  private Context mContext;
  private Database mDatabase;
}
