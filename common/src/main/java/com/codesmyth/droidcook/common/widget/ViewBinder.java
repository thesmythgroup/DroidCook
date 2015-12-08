package com.codesmyth.droidcook.common.widget;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class ViewBinder extends RecyclerView.ViewHolder {
  public ViewBinder(View itemView) {
    super(itemView);
  }

  public abstract void bind(AffixCursor cur, int pos);
}
