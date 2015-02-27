package com.codesmyth.android.common.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.codesmyth.android.common.util.RecyclerUtil;

public abstract class RecyclerFragment extends Fragment implements RecyclerUtil.OnItemClickListener {

  protected RecyclerView mRecycler;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mRecycler = new RecyclerView(inflater.getContext());
    mRecycler.setHasFixedSize(true);
    mRecycler.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
    mRecycler.setItemAnimator(new DefaultItemAnimator());
    mRecycler.setSoundEffectsEnabled(true);
    RecyclerUtil.GestureListener l = new RecyclerUtil.GestureListener(mRecycler, this);
    GestureDetectorCompat gd = new GestureDetectorCompat(inflater.getContext(), l);
    mRecycler.addOnItemTouchListener(new RecyclerUtil.ItemTouchListener(gd, l));
    return mRecycler;
  }
}
