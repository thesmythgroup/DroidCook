package com.codesmyth.android.common.widget;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class ViewBinder<A extends RecyclerView.Adapter> extends RecyclerView.ViewHolder {

    public ViewBinder(View itemView) {
        super(itemView);
    }

    public abstract void bind(A adapter, int pos);
}