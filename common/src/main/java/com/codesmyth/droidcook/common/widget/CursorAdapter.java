package com.codesmyth.droidcook.common.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings("unused")
@TargetApi(12)
public abstract class CursorAdapter extends RecyclerView.Adapter<ViewBinder> {

  public abstract Cursor exec();

  private AffixCursor mCursor;
  private RecyclerView mRecycler;

  private View.OnAttachStateChangeListener mAttachListener = new View.OnAttachStateChangeListener() {
    @Override
    public void onViewAttachedToWindow(View view) {
    }

    @Override
    public void onViewDetachedFromWindow(View view) {
      close();
    }
  };

  public CursorAdapter(Context context) {
    mCursor = new AffixCursor(context);
    setHasStableIds(true);
  }

  @Override
  public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
    mRecycler = recyclerView;
    mRecycler.addOnAttachStateChangeListener(mAttachListener);
  }

  @Override
  public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
    super.onDetachedFromRecyclerView(recyclerView);
    mRecycler.removeOnAttachStateChangeListener(mAttachListener);
    mRecycler = null;
    close();
  }

  @Override
  public int getItemViewType(int position) {
    return mCursor.getViewType(position);
  }

  @Override
  public void onBindViewHolder(ViewBinder binder, int position) {
// TODO(d) isNullOrEmpty before a moveToPositionOrThrow seems counter-intuitive. wrt empty views
//    if (!mCursor.isNullOrEmpty() && mCursor.getDomain().test(position) == AffixCursor.Domain.NO_MATCH) {
//      mCursor.moveToPositionOrThrow(position);
//    }
    binder.bind(mCursor, position);
  }

  @Override
  public int getItemCount() {
    return mCursor.getCount();
  }

  @Override
  public long getItemId(int position) {
    return mCursor.getId(position);
  }

  public void refresh() {
    if (mRecycler == null) {
      throw new IllegalStateException("CursorAdapter not attached to a RecyclerView; wont be able to finalize cursor.");
    }

    Cursor prev = mCursor.mCursor;
    mCursor.swap(exec());

    if (prev == null) {
      notifyItemRangeInserted(0, mCursor.getCount());
    } else {
      prev.close();
      notifyDataSetChanged();
    }
  }

  public void close() {
    mCursor.close();
    mCursor.swap(null);
  }

  public Context getContext() {
    return mCursor.getContext();
  }

  public AffixCursor getCursor() {
    return mCursor;
  }
}