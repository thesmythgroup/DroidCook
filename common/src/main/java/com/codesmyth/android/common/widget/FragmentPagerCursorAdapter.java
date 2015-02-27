package com.codesmyth.android.common.widget;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public abstract class FragmentPagerCursorAdapter extends FragmentStatePagerAdapter
    implements LoaderManager.LoaderCallbacks<Cursor> {

  private ConstraintCursor mCursor;
  private int              mLoaderId;

  public FragmentPagerCursorAdapter(FragmentManager fm, Context context) {
    super(fm);
    mCursor = new ConstraintCursor(context);
    mLoaderId = LoaderId.next();
  }

  public ConstraintCursor getCursor() {
    return mCursor;
  }

  public int getLoaderId() {
    return mLoaderId;
  }

  @Override
  public int getCount() {
    return mCursor.getCount();
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    if (mCursor != data) {
      mCursor.swap(data);
      notifyDataSetChanged();
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mCursor.swap(null);
    notifyDataSetChanged();
  }
}
