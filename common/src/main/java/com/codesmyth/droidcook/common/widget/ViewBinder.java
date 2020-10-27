package com.codesmyth.droidcook.common.widget;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public abstract class ViewBinder extends RecyclerView.ViewHolder {
  public ViewBinder(View itemView) {
    super(itemView);
  }

  public abstract void bind(AffixCursor cur, int pos);
}
