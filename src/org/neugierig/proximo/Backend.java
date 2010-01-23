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
  private static final String TAG = "proximo Backend";

  Backend(Context context) {
    mContext = context;
    mDatabase = new Database(context);
  }

  ProximoBus.Route[] fetchRoutes()
      throws MalformedURLException, IOException, JSONException
  {
    return ProximoBus.parseRoutes(queryAPI(ProximoBus.getAllRoutesPath(), false));
  }

  ProximoBus.Run[] fetchRunsOnRoute(String routeId)
      throws MalformedURLException, IOException, JSONException
  {
    return ProximoBus.parseRuns(queryAPI(ProximoBus.getRunsOnRoutePath(routeId), false));
  }

  ProximoBus.Stop[] fetchStopsOnRun(String routeId, String runId)
      throws MalformedURLException, IOException, JSONException
  {
    return ProximoBus.parseStops(queryAPI(ProximoBus.getStopsOnRunPath(routeId, runId), false));
  }

  ProximoBus.Prediction[] fetchPredictionsForRouteAtStop(String routeId, String stopId, boolean forceRefresh)
      throws MalformedURLException, IOException, JSONException
  {
    return ProximoBus.parsePredictions(queryAPI(ProximoBus.getStopPredictionsByRoutePath(stopId, routeId), forceRefresh));
  }

  String queryAPI(String path, boolean reload)
      throws MalformedURLException, IOException
  {
    String data = null;

    if (!reload)
      data = mDatabase.get(path);

    if (data == null) {
      data = ProximoBus.queryNetwork(path);
      Log.i(TAG, "Network fetch: " + data);
      mDatabase.put(path, data);
    }

    return data;
  }

  private Context mContext;
  private Database mDatabase;
}
