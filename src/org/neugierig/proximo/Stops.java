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
  private MuniAPI.Stop[] mStops;

  private String mRoute;
  private String mDirection;
  private String mQuery;
  private AsyncBackendHelper mBackendHelper;

  private class StopsQuery implements AsyncBackend.Query {
    final String mQuery;
    StopsQuery(String query) {
      mQuery = query;
    }
    public Object runQuery(Backend backend) throws Exception {
      return backend.fetchStops(mQuery);
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    super.onCreate(savedInstanceState);

    mQuery = getIntent().getExtras().getString(Backend.KEY_QUERY);
    mRoute = getIntent().getExtras().getString(Route.KEY_ROUTE);
    mDirection = getIntent().getExtras().getString(Route.KEY_DIRECTION);

    ListAdapter adapter = new ArrayAdapter<String>(
        this,
        android.R.layout.simple_list_item_1,
        new String[] {});
    setListAdapter(adapter);

    mBackendHelper = new AsyncBackendHelper(this, this);
    mBackendHelper.start(new StopsQuery(mQuery));
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    return mBackendHelper.onCreateDialog(id);
  }

  @Override
  public void onAsyncResult(Object data) {
    mStops = (MuniAPI.Stop[]) data;
    ListAdapter adapter = new ArrayAdapter<MuniAPI.Stop>(
        this,
        android.R.layout.simple_list_item_1,
        mStops);
    setListAdapter(adapter);
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    MuniAPI.Stop stop = mStops[position];
    Intent intent = new Intent(this, Stop.class);
    intent.putExtra(Route.KEY_ROUTE, mRoute);
    intent.putExtra(Route.KEY_DIRECTION, mDirection);
    intent.putExtra(Stop.KEY_NAME, stop.name);
    intent.putExtra(Backend.KEY_QUERY, stop.url);
    startActivity(intent);
  }
}
