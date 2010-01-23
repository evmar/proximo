// Copyright (c) 2010 Evan Martin.  All rights reserved.
// Use of this source code is governed by a BSD-style license that can
// be found in the LICENSE file.

package org.neugierig.proximo;

import android.app.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.util.Log;

public class Stop extends Activity implements AsyncBackendHelper.Delegate,
                                              View.OnClickListener {
  private String mRouteId;
  private String mRouteName;
  private String mRunId;
  private String mRunName;
  private String mStopId;
  private String mStopName;
  private ProximoBus.Prediction[] mPredictions;

  private AsyncBackendHelper mBackendHelper;
  private StarDBAdapter mStarDB;
  private CheckBox mStarView;

  private class PredictionsForStopQuery implements AsyncBackend.Query {
    final String mStopId;
    final String mRouteId;
    final boolean mForceRefresh;
    PredictionsForStopQuery(String routeId, String stopId, boolean forceRefresh) {
      mRouteId = routeId;
      mStopId = stopId;
      mForceRefresh = forceRefresh;
    }
    public Object runQuery(Backend backend) throws Exception {
      return backend.fetchPredictionsForRouteAtStop(mRouteId, mStopId, mForceRefresh);
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.stop);

    mStarDB = new StarDBAdapter(this);

    Bundle extras = getIntent().getExtras();
    mRouteId = extras.getString(ViewState.ROUTE_ID_KEY);
    mRouteName = extras.getString(ViewState.ROUTE_NAME_KEY);
    mRunId = extras.getString(ViewState.RUN_ID_KEY);
    mRunName = extras.getString(ViewState.RUN_NAME_KEY);
    mStopId = extras.getString(ViewState.STOP_ID_KEY);
    mStopName = extras.getString(ViewState.STOP_NAME_KEY);

    TextView title = (TextView) findViewById(R.id.title);
    title.setText(mStopName);
    TextView subtitle = (TextView) findViewById(R.id.subtitle);
    subtitle.setText(mRouteName + ": " + mRunName);

    mStarView = (CheckBox) findViewById(R.id.star);
    mStarView.setOnClickListener(this);
    mStarView.setChecked(mStarDB.isStopAFavorite(mRouteId, mStopId));

    mBackendHelper = new AsyncBackendHelper(this, this);
    mBackendHelper.start(new PredictionsForStopQuery(mRouteId, mStopId, false));
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    return mBackendHelper.onCreateDialog(id);
  }

  @Override
  public void onAsyncResult(Object data) {
    mPredictions = (ProximoBus.Prediction[]) data;

    ListView list = (ListView) findViewById(R.id.list);
    ListAdapter adapter;
    if (mPredictions.length > 0) {
      adapter = new ArrayAdapter<ProximoBus.Prediction>(
          this,
          android.R.layout.simple_list_item_1,
          mPredictions);
    } else {
      adapter = new ArrayAdapter<String>(
          this,
          android.R.layout.simple_list_item_1,
          new String[] {"(no arrivals predicted)"});
    }
    list.setAdapter(adapter);
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.star:
        if (mStarView.isChecked())
          mStarDB.addStopAsFavorite(mRouteId, mRouteName, mRunId, mRunName, mStopId, mStopName);
        else
          mStarDB.removeStopAsFavorite(mRouteId, mStopId);
        break;
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.stop_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.refresh:
      mBackendHelper.start(new PredictionsForStopQuery(mRouteId, mStopId, true));
      return true;
    }
    return false;
  }
}
