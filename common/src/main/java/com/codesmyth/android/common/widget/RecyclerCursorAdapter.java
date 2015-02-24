package com.codesmyth.android.common.widget;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RecyclerCursorAdapter<A extends RecyclerView.Adapter>
        extends RecyclerView.Adapter<ViewBinder<A>>
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private AtomicBoolean mHintRemoveInsert = new AtomicBoolean(false);

    private ConstraintCursor mCursor;
    private int mLoaderId;

    public RecyclerCursorAdapter(Context context) {
        super();
        mCursor = new ConstraintCursor(context);
        mLoaderId = LoaderId.next();
        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        return mCursor.getViewType(position);
    }

    @Override
    public void onBindViewHolder(ViewBinder<A> holder, int position) {
        if (mCursor.getDomain().test(position) == ConstraintCursor.Domain.NO_MATCH) {
            mCursor.moveToPositionOrThrow(position);
        }
        holder.bind((A) this, position);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        return mCursor.getId(position);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mCursor != data) {
            if (mHintRemoveInsert.compareAndSet(true, false)) {
                notifyItemRangeRemoved(0, getItemCount());
                mCursor.swap(data);
                notifyItemRangeInserted(0, getItemCount());
            } else {
                mCursor.swap(data);
                notifyItemRangeChanged(0, getItemCount());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int count = mCursor.getCount();
        mCursor.swap(null);
        if (mHintRemoveInsert.get()) {
            notifyItemRangeRemoved(0, count);
        }
    }

    public ConstraintCursor getCursor() {
        return mCursor;
    }

    public Context getContext() {
        return mCursor.getContext();
    }

    public int getLoaderId() {
        return mLoaderId;
    }

    /**
     * Affects behaviour when resetting loader manager, acting as though all items were removed and
     * upon cursor re-query, all items were freshly inserted. Effectively resets the adapter for a
     * single loader manager reset and is then disabled until this method is called again.
     */
    public void hintRemoveInsert() {
        mHintRemoveInsert.set(true);
    }
}
