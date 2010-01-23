// Copyright (c) 2010 Evan Martin.  All rights reserved.
// Use of this source code is governed by a BSD-style license that can
// be found in the LICENSE file.

package org.neugierig.proximo;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.util.Log;

public class StarDBAdapter {
  private SQLiteDatabase mDb;
  private static final String TAG = "StarDBAdapter";

  private static class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favorites";
    private static final int DATABASE_VERSION = 1;

    DatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("create table stops (_id integer primary key autoincrement, " +
                 "route_id text, route_name text, run_id text, run_name text, stop_id text, stop_name text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(TAG, "Upgrading database from version " + oldVersion + " to " +
            newVersion + ", which will destroy all old data");
      db.execSQL("DROP TABLE IF EXISTS stops");
      onCreate(db);
    }
  }

  public StarDBAdapter(Context context) throws SQLException {
    mDb = (new DatabaseHelper(context)).getWritableDatabase();
  }

  public boolean isStopAFavorite(String routeId, String stopId) {
    Cursor cursor = mDb.rawQuery("select * from stops where route_id=? and stop_id=?",
                                 new String[] {routeId, stopId});
    boolean isFavorite = cursor.getCount() > 0;
    cursor.close();
    return isFavorite;
  }

  public void addStopAsFavorite(String routeId, String routeName, String runId, String runName, String stopId, String stopName) {
    ContentValues values = new ContentValues();
    values.put("route_id", routeId);
    values.put("route_name", routeName);
    values.put("run_id", runId);
    values.put("run_name", runName);
    values.put("stop_id", stopId);
    values.put("stop_name", stopName);
    mDb.replace("stops", null, values);
  }

  public void removeStopAsFavorite(String routeId, String stopId) {
    mDb.execSQL("delete from stops where route_id=? and stop_id=?", new Object[] {routeId, stopId});
  }

  public Cursor fetchAll() {
    return mDb.query("stops",
                     new String[] {"_id", "route_id", "route_name", "run_id", "run_name", "stop_id", "stop_name"},
                     null, null, null, null, null);
  }
}
