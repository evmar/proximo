// Copyright (c) 2010 Evan Martin.  All rights reserved.
// Use of this source code is governed by a BSD-style license that can
// be found in the LICENSE file.

package org.neugierig.proximo;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.util.Log;

public class Database {
  private static final String TAG = "proxmio Database";
  private static final String FILE = "queries.db";
  private static final String TABLE = "queries";
  private static final int VERSION = 1;

  private static class Helper extends SQLiteOpenHelper {
    Helper(Context context) {
      super(context, FILE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE " + TABLE
               + "( query STRING PRIMARY KEY"
               + ", json STRING"
            // + ", lastfetch INTEGER"
               + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
      db.execSQL("DROP TABLE IF EXISTS " + TABLE);
      onCreate(db);
    }
  }

  Database(Context context) {
    mHelper = new Helper(context);
  }

  String get(String query) {
    SQLiteDatabase db = mHelper.getReadableDatabase();
    Cursor cur = db.query(TABLE, new String[] {"json"},
                          "query = ?", new String[] { query },
                          null, null, null);
    if (!cur.moveToFirst())
      return null;
    return cur.getString(0);
  }

  void put(String query, String json) {
    SQLiteDatabase db = mHelper.getWritableDatabase();
    ContentValues args = new ContentValues();
    args.put("query", query);
    args.put("json", json);
    db.replace(TABLE, "query", args);
  }

  private Helper mHelper;
}
