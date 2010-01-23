// Copyright (c) 2010 Evan Martin.  All rights reserved.
// Use of this source code is governed by a BSD-style license that can
// be found in the LICENSE file.

package org.neugierig.proximo;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.widget.*;
import android.view.*;

public class Stops extends ListActivity implements AsyncBackendHelper.Delegate {
  private ProximoBus.Stop[] mStops;

  private String mRouteName;
  private String mRouteId;
  private String mRunName;
  private String mRunId;
  private AsyncBackendHelper mBackendHelper;

  private class StopsOnRunQuery implements AsyncBackend.Query {
    final String mRouteId;
    final String mRunId;
    StopsOnRunQuery(String routeId, String runId) {
      mRouteId = routeId;
      mRunId = runId;
    }
    public Object runQuery(Backend backend) throws Exception {
      return backend.fetchStopsOnRun(mRouteId, mRunId);
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    super.onCreate(savedInstanceState);

    mRouteName = getIntent().getExtras().getString(ViewState.ROUTE_NAME_KEY);
    mRouteId = getIntent().getExtras().getString(ViewState.ROUTE_ID_KEY);
    mRunName = getIntent().getExtras().getString(ViewState.RUN_NAME_KEY);
    mRunId = getIntent().getExtras().getString(ViewState.RUN_ID_KEY);

    ListAdapter adapter = new ArrayAdapter<String>(
        this,
        android.R.layout.simple_list_item_1,
        new String[] {});
    setListAdapter(adapter);

    mBackendHelper = new AsyncBackendHelper(this, this);
    mBackendHelper.start(new StopsOnRunQuery(mRouteId, mRunId));
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    return mBackendHelper.onCreateDialog(id);
  }

  @Override
  public void onAsyncResult(Object data) {
    mStops = (ProximoBus.Stop[]) data;
    ListAdapter adapter = new ArrayAdapter<ProximoBus.Stop>(
        this,
        android.R.layout.simple_list_item_1,
        mStops);
    setListAdapter(adapter);
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    ProximoBus.Stop stop = mStops[position];
    Intent intent = new Intent(this, Stop.class);
    intent.putExtra(ViewState.ROUTE_ID_KEY, mRouteId);
    intent.putExtra(ViewState.ROUTE_NAME_KEY, mRouteName);
    intent.putExtra(ViewState.RUN_ID_KEY, mRunId);
    intent.putExtra(ViewState.RUN_NAME_KEY, mRunName);
    intent.putExtra(ViewState.STOP_ID_KEY, stop.id);
    intent.putExtra(ViewState.STOP_NAME_KEY, stop.displayName);
    startActivity(intent);
  }
}
