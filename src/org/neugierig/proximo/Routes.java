// Copyright (c) 2010 Evan Martin.  All rights reserved.
// Use of this source code is governed by a BSD-style license that can
// be found in the LICENSE file.

package org.neugierig.proximo;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class Routes extends ListActivity
                    implements AsyncBackendHelper.Delegate {
  private MuniAPI.Route[] mRoutes;
  private SplitListAdapter mSplitListAdapter;
  private AsyncBackendHelper mBackendHelper;
  private StarDBAdapter mStarDB;
  private Cursor mCursor;

  private class RoutesQuery implements AsyncBackend.Query {
    public Object runQuery(Backend backend) throws Exception {
      return backend.fetchRoutes();
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    super.onCreate(savedInstanceState);

    mStarDB = new StarDBAdapter(this);

    mSplitListAdapter = new SplitListAdapter(this, "All Routes");
    fillStarred();
    setListAdapter(mSplitListAdapter);

    mBackendHelper = new AsyncBackendHelper(this, this);
    mBackendHelper.start(new RoutesQuery());
  }

  @Override
  public void onResume() {
    super.onResume();
    // When they've gone off in another Activity and changed the starring
    // of entries, we want to reflect that here immediately whenever we
    // reappear.
    fillStarred();
  }

  private void fillStarred() {
    mCursor = mStarDB.fetchAll();
    startManagingCursor(mCursor);

    String[] from = new String[]{"name", "route"};
    int[] to = new int[]{android.R.id.text1, android.R.id.text2};
    SimpleCursorAdapter notes = new SimpleCursorAdapter(
        this,
        R.layout.starred_item,
        mCursor, from, to);
    mSplitListAdapter.setAdapter1(notes);
    // Force view to reload adapter; works around a crash.  :(
    setListAdapter(mSplitListAdapter);
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    return mBackendHelper.onCreateDialog(id);
  }

  @Override
  public void onAsyncResult(Object data) {
    mRoutes = (MuniAPI.Route[]) data;
    ListAdapter adapter = new ArrayAdapter<MuniAPI.Route>(
        this,
        android.R.layout.simple_list_item_1,
        mRoutes);
    mSplitListAdapter.setAdapter2(adapter);
    // Force view to reload adapter; works around a crash.  :(
    setListAdapter(mSplitListAdapter);
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    if (mSplitListAdapter.isInList1(position)) {
      mCursor.moveToPosition(position);
      Intent intent = new Intent(this, Stop.class);
      intent.putExtra(Route.KEY_ROUTE,
                      mCursor.getString(mCursor.getColumnIndexOrThrow("route")));
      intent.putExtra(Route.KEY_DIRECTION,
                      mCursor.getString(mCursor.getColumnIndexOrThrow("direction")));
      intent.putExtra(Stop.KEY_NAME,
                      mCursor.getString(mCursor.getColumnIndexOrThrow("name")));
      intent.putExtra(Backend.KEY_QUERY,
                      mCursor.getString(mCursor.getColumnIndexOrThrow("query")));
      startActivity(intent);
    } else {
      MuniAPI.Route route =
          mRoutes[mSplitListAdapter.translateList2Position(position)];
      Intent intent = new Intent(this, Route.class);
      intent.putExtra(Route.KEY_ROUTE, route.name);
      intent.putExtra(Backend.KEY_QUERY, route.url);
      startActivity(intent);
    }
  }
}
