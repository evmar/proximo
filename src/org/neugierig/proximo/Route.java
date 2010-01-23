// Copyright (c) 2010 Evan Martin.  All rights reserved.
// Use of this source code is governed by a BSD-style license that can
// be found in the LICENSE file.

package org.neugierig.proximo;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.widget.*;
import android.view.*;

public class Route extends ListActivity implements AsyncBackendHelper.Delegate {
  private String mRouteId;
  private String mRouteName;
  private ProximoBus.Run[] mRuns;
  private AsyncBackendHelper mBackendHelper;

  private class RunsQuery implements AsyncBackend.Query {
    final String mRouteId;
    RunsQuery(String routeId) {
      mRouteId = routeId;
    }
    public Object runQuery(Backend backend) throws Exception {
      return backend.fetchRunsOnRoute(mRouteId);
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    super.onCreate(savedInstanceState);

    mRouteId = getIntent().getExtras().getString(ViewState.ROUTE_ID_KEY);
    mRouteName = getIntent().getExtras().getString(ViewState.ROUTE_NAME_KEY);

    ListAdapter adapter = new ArrayAdapter<String>(
        this,
        android.R.layout.simple_list_item_1,
        new String[] {});
    setListAdapter(adapter);

    mBackendHelper = new AsyncBackendHelper(this, this);
    mBackendHelper.start(new RunsQuery(mRouteId));
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    return mBackendHelper.onCreateDialog(id);
  }

  @Override
  public void onAsyncResult(Object data) {
    mRuns = (ProximoBus.Run[]) data;
    ListAdapter adapter = new ArrayAdapter<ProximoBus.Run>(
        this,
        android.R.layout.simple_list_item_1,
        mRuns);
    setListAdapter(adapter);
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    ProximoBus.Run run = mRuns[position];
    Intent intent = new Intent(this, Stops.class);
    intent.putExtra(ViewState.ROUTE_NAME_KEY, mRouteName);
    intent.putExtra(ViewState.ROUTE_ID_KEY, mRouteId);
    intent.putExtra(ViewState.RUN_NAME_KEY, run.displayName);
    intent.putExtra(ViewState.RUN_ID_KEY, run.id);
    startActivity(intent);
  }
}
