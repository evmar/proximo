// Copyright (c) 2010 Evan Martin.  All rights reserved.
// Use of this source code is governed by a BSD-style license that can
// be found in the LICENSE file.

package org.neugierig.proximo;

import android.app.*;
import android.content.*;
import android.util.Log;

// AsyncBackendHelper provides a UI-side counterpart to AsyncBackend.
// It has hooks into (and expects callbacks from) an Activity and
// manages displaying the "Network Error" dialog.
class AsyncBackendHelper implements AsyncBackend.APIResultCallback {
  public interface Delegate {
    // Return the result of a query.  If there was an error, it will
    // never get called.
    public void onAsyncResult(Object obj);
  }

  AsyncBackendHelper(Activity activity, Delegate delegate) {
    mActivity = activity;
    mDelegate = delegate;
    mBackend = new AsyncBackend(activity);
  }

  public void start(AsyncBackend.Query query) {
    mQuery = query;
    restart();
  }

  public void restart() {
    mActivity.setProgressBarIndeterminateVisibility(true);
    mBackend.startQuery(mQuery, this);
  }

  @Override
  public void onAPIResult(Object obj) {
    mActivity.setProgressBarIndeterminateVisibility(false);
    mDelegate.onAsyncResult(obj);
  }

  @Override
  public void onException(Exception exn) {
    mActivity.setProgressBarIndeterminateVisibility(false);
    mBackendError = exn;
    mActivity.showDialog(ERROR_DIALOG_ID);
  }

  public Dialog onCreateDialog(int id) {
    switch (id) {
      case ERROR_DIALOG_ID: {
        DialogInterface.OnClickListener clicker =
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                  case DialogInterface.BUTTON1:
                    restart();
                    break;
                  case DialogInterface.BUTTON2:
                    mActivity.dismissDialog(ERROR_DIALOG_ID);
                    mActivity.finish();
                    break;
                }
              }
            };

        return new AlertDialog.Builder(mActivity)
            .setTitle("Server Error")
            .setMessage(mBackendError.getLocalizedMessage())
            .setPositiveButton("Retry", clicker)
            .setNegativeButton("Cancel", clicker)
            .create();
      }
    }

    return null;
  }

  private static final int ERROR_DIALOG_ID = 0;     // XXX how to choose?
  private Activity mActivity;
  private Delegate mDelegate;
  private AsyncBackend mBackend;
  private Exception mBackendError;
  private AsyncBackend.Query mQuery;
}
