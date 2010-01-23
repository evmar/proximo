// Copyright (c) 2010 Evan Martin.  All rights reserved.
// Use of this source code is governed by a BSD-style license that can
// be found in the LICENSE file.

package org.neugierig.proximo;

import android.content.Context;
import android.util.Log;
import android.view.*;
import android.widget.*;

class SplitListAdapter extends BaseAdapter {
  private Context mContext;
  private int mSeparatorPos = ListView.INVALID_POSITION;
  private String mSeparatorText;
  private View mSeparatorView;
  private ListAdapter mList1, mList2;

  SplitListAdapter(Context context, String text) {
    mContext = context;
    mSeparatorText = text;
  }

  void setAdapter1(ListAdapter l1) {
    mList1 = l1;
    mSeparatorPos = l1.getCount();
    notifyDataSetChanged();
  }
  void setAdapter2(ListAdapter l2) {
    mList2 = l2;
    notifyDataSetChanged();
  }

  boolean isInList1(int position) {
    if (!hasList2())
      return true;
    return position <= mSeparatorPos;
  }
  // Translate a global position to a position within the second list.
  int translateList2Position(int position) {
    return position - mSeparatorPos - 1;
  }

  private boolean hasList2() {
    return mList2 != null;
  }
  private boolean hasSeparator() {
    return mSeparatorPos != ListView.INVALID_POSITION;
  }

  // ListAdapter
  @Override public boolean areAllItemsEnabled() { return !hasSeparator(); }
  @Override public boolean isEnabled(int pos) { return pos != mSeparatorPos; }

  // Adapter
  @Override public int getCount() {
    if (!hasList2())
      return mList1.getCount();
    return mList1.getCount() + 1 + mList2.getCount();
  }
  @Override public Object getItem(int pos) {
    if (!hasList2())
      return mList1.getItem(pos);

    if (pos < mSeparatorPos) {
      return mList1.getItem(pos);
    } else if (pos == mSeparatorPos) {
      return null;
    } else {
      return mList2.getItem(translateList2Position(pos));
    }
  }
  @Override public long getItemId(int pos) {
    // XXX I don't understand the purpose of this function.
    return pos;
  }
  @Override public int getItemViewType(int pos) {
    if (!hasList2() || pos < mSeparatorPos) {
      return 1 + mList1.getItemViewType(pos);
    } else if (pos == mSeparatorPos) {
      return 0;
    } else {
      return 1 + mList1.getViewTypeCount() +
          mList2.getItemViewType(translateList2Position(pos));
    }
  }
  @Override public View getView(int pos, View view, ViewGroup parent) {
    if (!hasList2())
      return mList1.getView(pos, view, parent);

    if (pos < mSeparatorPos) {
      return mList1.getView(pos, view, parent);
    } else if (pos == mSeparatorPos) {
      if (view != null)
        return view;
      LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
          Context.LAYOUT_INFLATER_SERVICE);
      TextView textview = (TextView) inflater.inflate(R.layout.separator,
                                                      parent, false);
      textview.setText(mSeparatorText);
      return textview;
    } else {
      return mList2.getView(translateList2Position(pos), view, parent);
    }
  }
  @Override public int getViewTypeCount() {
    if (!hasList2())
      return 1 + mList1.getViewTypeCount();
    return 1 + mList1.getViewTypeCount() + mList2.getViewTypeCount();
  }
  @Override public boolean hasStableIds() {
    if (!hasList2())
      return mList1.hasStableIds();

    return mList1.hasStableIds() && mList2.hasStableIds();
  }
  @Override public boolean isEmpty() {
    if (!hasList2())
      return mList1.isEmpty();

    return mList1.isEmpty() && mList2.isEmpty();
  }
}
