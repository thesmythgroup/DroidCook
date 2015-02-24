package com.codesmyth.android.common.app;

import android.app.Activity;
import android.os.Build;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;

public class DrawerTogglePushContent extends ActionBarDrawerToggle {

    private ViewGroup mDrawer;
    private ViewGroup mContent;

    private Float mLastTranslate = 0f;

    public DrawerTogglePushContent(Activity act, DrawerLayout layout, ViewGroup drawer, ViewGroup content, int imageRes, int openDescRes, int closeDescRes) {
        super(act, layout, imageRes, openDescRes, closeDescRes);
        mDrawer = drawer;
        mContent = content;
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        float moveFactor = mDrawer.getWidth() * slideOffset;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mContent.setTranslationX(moveFactor);
        } else {
            TranslateAnimation anim = new TranslateAnimation(mLastTranslate, moveFactor, 0f, 0f);
            anim.setDuration(0);
            anim.setFillAfter(true);
            mContent.startAnimation(anim);
            mLastTranslate = moveFactor;
        }
    }
}
