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
  // Intent extra data on the stop name.
  public static final String KEY_NAME = "name";

  private MuniAPI.Stop mStop;
  private String mRoute;
  private String mDirection;
  private AsyncBackendHelper mBackendHelper;
  private StarDBAdapter mStarDB;
  private CheckBox mStarView;

  private class StopQuery implements AsyncBackend.Query {
    final String mQuery;
    final boolean mReload;
    StopQuery(String query, boolean reload) {
      mQuery = query;
      mReload = reload;
    }
    public Object runQuery(Backend backend) throws Exception {
      return backend.fetchStop(mQuery, mReload);
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.stop);

    mStarDB = new StarDBAdapter(this);

    Bundle extras = getIntent().getExtras();
    mRoute = extras.getString(Route.KEY_ROUTE);
    mDirection = extras.getString(Route.KEY_DIRECTION);
    mStop = new MuniAPI.Stop(extras.getString(KEY_NAME),
                             extras.getString(Backend.KEY_QUERY));

    TextView title = (TextView) findViewById(R.id.title);
    title.setText(mStop.name);
    TextView subtitle = (TextView) findViewById(R.id.subtitle);
    subtitle.setText(mRoute + ": " + mDirection);

    mStarView = (CheckBox) findViewById(R.id.star);
    mStarView.setOnClickListener(this);
    mStarView.setChecked(mStarDB.getStarred(mStop.url));

    mBackendHelper = new AsyncBackendHelper(this, this);
    mBackendHelper.start(new StopQuery(mStop.url, false));
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    return mBackendHelper.onCreateDialog(id);
  }

  @Override
  public void onAsyncResult(Object data) {
    mStop.times = (MuniAPI.Stop.Time[]) data;

    ListView list = (ListView) findViewById(R.id.list);
    ListAdapter adapter;
    if (mStop.times.length > 0) {
      adapter = new ArrayAdapter<MuniAPI.Stop.Time>(
          this,
          android.R.layout.simple_list_item_1,
          mStop.times);
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
          mStarDB.setStarred(mStop, mRoute, mDirection);
        else
          mStarDB.unStar(mStop);
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
      mBackendHelper.start(new StopQuery(mStop.url, true));
      return true;
    }
    return false;
  }
}
