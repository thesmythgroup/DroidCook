package com.codesmyth.droidcook.common.widget;

import android.support.v7.widget.RecyclerView;
import android.view.View;

@Deprecated
public abstract class RecyclerViewBinder<A extends RecyclerView.Adapter> extends RecyclerView.ViewHolder {

  public RecyclerViewBinder(View itemView) {
    super(itemView);
  }

  public abstract void bind(A adapter, int pos);
}