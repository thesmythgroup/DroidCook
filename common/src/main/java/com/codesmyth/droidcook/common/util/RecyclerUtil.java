package com.codesmyth.droidcook.common.util;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

public class RecyclerUtil {

  public static void setOnItemClickListener(RecyclerView view, OnItemClickListener listener) {
    view.setSoundEffectsEnabled(true);
    GestureListener l = new GestureListener(view, listener);
    GestureDetectorCompat gd = new GestureDetectorCompat(view.getContext(), l);
    view.addOnItemTouchListener(new ItemTouchListener(gd, l));
  }

  public static class GestureListener implements GestureDetector.OnGestureListener {

    private RecyclerView        mRoot;
    private OnItemClickListener mListener;
    private View                mSelection;

    public GestureListener(RecyclerView root, OnItemClickListener listener) {
      mRoot = root;
      mListener = listener;
    }

    private void setSelection(View view) {
      if (mSelection != null) {
        mSelection.setSelected(false);
      }
      mSelection = view;
      if (mSelection != null) {
        mSelection.setSelected(true);
      }
    }

    private View select(MotionEvent e) {
      return mRoot.findChildViewUnder(e.getX(), e.getY());
    }

    @Override
    public boolean onDown(MotionEvent e) {
      return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
      setSelection(select(e));
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      setSelection(select(e));
      if (mSelection != null) {
        if (mSelection.isSoundEffectsEnabled()) {
          mSelection.playSoundEffect(SoundEffectConstants.CLICK);
        }
        if (mListener != null) {
          int pos = mRoot.getChildPosition(mSelection);
          mListener.onItemClick(pos);
        }
      }
      return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      setSelection(null);
      return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      setSelection(null);
      return true;
    }
  }

  public static interface OnItemClickListener {
    public void onItemClick(int pos);
  }

  public static class ItemTouchListener implements RecyclerView.OnItemTouchListener {

    GestureDetectorCompat             mDetector;
    GestureDetector.OnGestureListener mListener;

    public ItemTouchListener(GestureDetectorCompat detector, GestureDetector.OnGestureListener listener) {
      mDetector = detector;
      mListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
      if (!mDetector.onTouchEvent(e)) {
        if (e.getAction() == MotionEvent.ACTION_UP) {
          mListener.onScroll(null, null, -1, -1);
        }
      }
      return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }
  }

  /**
   * https://code.google.com/p/android/issues/detail?id=78191
   */
  public static void fix78191(RecyclerView recycler, final SwipeRefreshLayout swipeRefresh, final RecyclerView.OnScrollListener onScrollListener) {
    recycler.setOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (onScrollListener != null) {
          onScrollListener.onScrollStateChanged(recyclerView, newState);
        }
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
          swipeRefresh.setEnabled(isAtTop(recyclerView));
        }
      }

      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (onScrollListener != null) {
          onScrollListener.onScrolled(recyclerView, dx, dy);
        }
        swipeRefresh.setEnabled(isAtTop(recyclerView));
      }

      private boolean isAtTop(RecyclerView recyclerView) {
        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
        if (lm instanceof LinearLayoutManager) {
          return ((LinearLayoutManager) lm).findFirstCompletelyVisibleItemPosition() == 0;
        } else if (lm instanceof StaggeredGridLayoutManager) {
          int[] positions = new int[10];
          ((StaggeredGridLayoutManager) lm).findFirstCompletelyVisibleItemPositions(positions);
          return positions.length != 0 && positions[0] == 0;
        }
        throw new IllegalStateException("Recycler has unknown layout manager.");
      }
    });
  }

  private RecyclerUtil() {
  }
}
