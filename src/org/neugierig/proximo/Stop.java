// Copyright (c) 2010 Evan Martin.  All rights reserved.
// Use of this source code is governed by a BSD-style license that can
// be found in the LICENSE file.

package org.neugierig.proximo;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.util.Log;

public class Stop extends Activity implements View.OnClickListener {
  private String mRouteId;
  private String mRouteName;
  private String mRunId;
  private String mRunName;
  private String mStopId;
  private String mStopName;
  private ProximoBus.Prediction[] mPredictions;

  private StarDBAdapter mStarDB;
  private CheckBox mStarView;

  private IUpdateService mService;

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

    // Turn on the in-progress throbber to show that we're continually fetching
    // new stop info.
    setProgressBarIndeterminateVisibility(true);
  }

  @Override
  public void onStart() {
    super.onStart();
    bindService(new Intent(this, UpdateService.class), mConnection,
                Context.BIND_AUTO_CREATE);
  }
  @Override
  public void onStop() {
    super.onStop();
    unbindService(mConnection);
  }

  private IUpdateMonitor.Stub mUpdateCallback = new IUpdateMonitor.Stub() {
      @Override
      public void onNewPredictions(ProximoBus.Prediction[] predictions) {
        mHandler.sendMessage(
            mHandler.obtainMessage(MSG_NEW_PREDICTIONS, predictions));
      }
    };

  private ServiceConnection mConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName className,
                                     IBinder service) {
        mService = IUpdateService.Stub.asInterface(service);
        try {
          mService.monitorStop(mRouteId, mStopId, mUpdateCallback);
        } catch (RemoteException e) {
          // In this case the service has crashed before we could even
          // do anything with it; we can count on soon being
          // disconnected (and then reconnected if it can be
          // restarted) so there is no need to do anything here.
        }
      }

      @Override
      public void onServiceDisconnected(ComponentName className) {
        mService = null;
      }
    };

  private final static int MSG_NEW_PREDICTIONS = 0;
  private Handler mHandler = new Handler() {
      @Override public void handleMessage(Message msg) {
        switch (msg.what) {
          case MSG_NEW_PREDICTIONS:
            showPredictions((ProximoBus.Prediction[])msg.obj);
            break;
          default:
            super.handleMessage(msg);
        }
      }
    };

  public void showPredictions(ProximoBus.Prediction[] predictions) {
    mPredictions = predictions;

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
}
