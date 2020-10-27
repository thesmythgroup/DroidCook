package com.codesmyth.droidcook.common.util;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class RecyclerUtil {
    public static void asPager(final RecyclerView view, int orientation, boolean reverseLayout) {
        final PagerLayoutManager lm = new PagerLayoutManager(view.getContext(), orientation, reverseLayout);
        view.setLayoutManager(lm);
        view.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
        final GestureDetector.SimpleOnGestureListener l = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (velocityX <= 0) {
                    view.smoothScrollToPosition(lm.findLastVisibleItemPosition());
                } else if (velocityX > 0) {
                    view.smoothScrollToPosition(lm.findFirstVisibleItemPosition());
                }
                return true;
            }
        };
        final GestureDetectorCompat gd = new GestureDetectorCompat(view.getContext(), l);
        view.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if (!gd.onTouchEvent(e)) {
                    if (e.getAction() == MotionEvent.ACTION_UP) {
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                view.smoothScrollToPosition(lm.findLastVisibleItemPosition());
                            }
                        });
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
    }

    public static class PagerLayoutManager extends LinearLayoutManager {
        public PagerLayoutManager(Context context) {
            super(context);
        }

        public PagerLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public PagerLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            LinearSmoothScroller linearSmoothScroller =
                    new LinearSmoothScroller(recyclerView.getContext()) {
                        @Override
                        public PointF computeScrollVectorForPosition(int targetPosition) {
                            return PagerLayoutManager.this.computeScrollVectorForPosition(targetPosition);
                        }

                        @Override
                        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                            return 50.0f / displayMetrics.densityDpi;
                        }
                    };
            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }
    }

    public static void setOnItemClickListener(RecyclerView view, OnItemClickListener listener) {
        view.setSoundEffectsEnabled(true);
        GestureListener l = new GestureListener(view, listener);
        GestureDetectorCompat gd = new GestureDetectorCompat(view.getContext(), l);
        view.addOnItemTouchListener(new ItemTouchListener(gd, l));
    }

    public static class GestureListener implements GestureDetector.OnGestureListener {

        private RecyclerView mRoot;
        private OnItemClickListener mListener;
        private View mSelection;

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

    public interface OnItemClickListener {
        void onItemClick(int pos);
    }

    public static class ItemTouchListener implements RecyclerView.OnItemTouchListener {

        GestureDetectorCompat mDetector;
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

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean b) {
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
