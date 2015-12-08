package com.codesmyth.droidcook.common.widget;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;

@Deprecated
public abstract class PagerCursorAdapter extends PagerAdapter
    implements LoaderManager.LoaderCallbacks<Cursor>, Serializable {

  private AffixCursor mCursor;
  private int         mLoaderId;

  public PagerCursorAdapter(Context context) {
    mCursor = new AffixCursor(context);
    mLoaderId = LoaderId.next();
  }

  public AffixCursor getCursor() {
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
  public boolean isViewFromObject(View view, Object object) {
    return object.equals(view);
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView(((View) object));
  }

  @Override
  public int getItemPosition(Object object) {
    return PagerAdapter.POSITION_NONE;
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
