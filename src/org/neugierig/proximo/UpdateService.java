// Copyright (c) 2010 Evan Martin.  All rights reserved.
// Use of this source code is governed by a BSD-style license that can
// be found in the LICENSE file.

package org.neugierig.proximo;

import android.app.*;
import android.content.*;
import android.os.*;

import android.util.Log;

// The UpdateService monitors stops for updates.
//
// It has two major modes of usage:
// 1) When a Stop Activity is showing, it binds to the service to
//    get updates on the currently-displayed stop.
// 2) When the user asks to monitor a stop, the Service runs in
//    the background and keeps a Notification updated.
//    (Not yet implemented.)
//
// Lifetime: when displaying the activity, we use the IBinder interface
// to tie the lifetime of the service to the activity; when the activity
// goes away it drops the only reference to the service, killing it.

public class UpdateService extends Service {
  // The route/stop we're currently monitoring, and the callback to fire.
  private String mRouteId;
  private String mStopId;
  private IUpdateMonitor mCallback;

  // Whether we've currently got a timeout enqueued; used to be extra sure
  // we don't poll too often.
  private boolean mAwaitingTimeout = false;

  // How frequently to poll.
  private static final int UPDATE_INTERVAL_SECONDS = 20;

  private AsyncBackend mBackend = new AsyncBackend(this);

  private class PredictionsForStopQuery implements AsyncBackend.Query {
    final String mRouteId;
    final String mStopId;
    final boolean mForceRefresh;
    PredictionsForStopQuery(String routeId, String stopId,
                            boolean forceRefresh) {
      mRouteId = routeId;
      mStopId = stopId;
      mForceRefresh = forceRefresh;
    }
    public Object runQuery(Backend backend) throws Exception {
      return backend.fetchPredictionsForRouteAtStop(mRouteId, mStopId,
                                                    mForceRefresh);
    }
  }

  private final IUpdateService.Stub mStub = new IUpdateService.Stub() {
      public void monitorStop(String routeId, String stopId,
                              IUpdateMonitor callback) {
        startMonitoring(routeId, stopId, callback);
      }
    };

  private void startMonitoring(String routeId, String stopId,
                               IUpdateMonitor callback) {
    // XXX insert into a table of interested callees
    mRouteId = routeId;
    mStopId = stopId;
    mCallback = callback;

    fireTimer(false);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mStub;
  }

  private static final int MSG_TIMEOUT = 0;
  private Handler mHandler = new Handler() {
      @Override public void handleMessage(Message msg) {
        switch (msg.what) {
          case MSG_TIMEOUT:
            mAwaitingTimeout = false;
            fireTimer(true);
            break;
          default:
            super.handleMessage(msg);
        }
      }
    };
  private void registerPollTimeoutIn(int seconds) {
    if (mAwaitingTimeout)
      return;

    mAwaitingTimeout = true;
    mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TIMEOUT, null),
                                seconds * 1000);
  }

  private AsyncBackend.APIResultCallback mBackendCallback =
      new AsyncBackend.APIResultCallback() {
    @Override public void onAPIResult(Object obj) {
      ProximoBus.Prediction[] predictions = (ProximoBus.Prediction[]) obj;
      try {
        mCallback.onNewPredictions(predictions);
      } catch (RemoteException e) {
        // They went away.
        mCallback = null;
        return;
      }
      registerPollTimeoutIn(UPDATE_INTERVAL_SECONDS);
    }
    @Override public void onException(Exception exn) {
      // XXX What can we do here?  Should we proxy the error message
      // back to any registered listeners?
      registerPollTimeoutIn(UPDATE_INTERVAL_SECONDS);
    }
  };

  private void fireTimer(boolean refresh) {
    mBackend.startQuery(new PredictionsForStopQuery(mRouteId, mStopId, refresh),
                        mBackendCallback);
  }
}
