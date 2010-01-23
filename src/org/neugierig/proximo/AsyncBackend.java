// Copyright (c) 2010 Evan Martin.  All rights reserved.
// Use of this source code is governed by a BSD-style license that can
// be found in the LICENSE file.

package org.neugierig.proximo;

import android.content.*;
import android.os.*;
import android.widget.Toast;
import org.json.*;

// The AsyncBackend provides an asynchronous interface to the
// (blocking) Backend.  Its callbacks always come back on the thread
// it's queried from, but internally it's running the queries on a
// separate thread.
class AsyncBackend {
  public interface APIResultCallback {
    // A query can either result in a positive response or an
    // exception.  Any exception that happens in the backend is
    // proxied over to onException here.
    public void onAPIResult(Object obj);
    public void onException(Exception exn);
  }

  // A Query is a snippet of code that is passed to the backend
  // and run.  Its resulting object comes back asynchronously via
  // the APIResultCallback interface.
  public interface Query {
    public Object runQuery(Backend backend) throws Exception;
  }

  AsyncBackend(Context context) {
    mContext = context;
    mBackend = new Backend(context);
  }

  // All the thread magic happens in this single function, which
  // hopefully helps reasoning about what needs locks.  TODO: maybe
  // lock around not having two pending queries out at once, which
  // doesn't make sense from an API perspective but could race in
  // mBackend.
  public void startQuery(final Query query,
                         final APIResultCallback callback) {
    final int MSG_RESULT = 0;
    final int MSG_EXCEPTION = 1;

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
          switch (msg.what) {
          case MSG_RESULT:
            callback.onAPIResult(msg.obj);
            break;
          case MSG_EXCEPTION:
            callback.onException((Exception)msg.obj);
            break;
          }
        }
      };

    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          Object result = query.runQuery(mBackend);
          handler.sendMessage(handler.obtainMessage(MSG_RESULT, result));
        } catch (Exception exn) {
          handler.sendMessage(handler.obtainMessage(MSG_EXCEPTION,
                                                    (Object)exn));
        }
      }
    }, "Network Fetch");
    thread.start();
  }

  private Context mContext;
  private Backend mBackend;
}
