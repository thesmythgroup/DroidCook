package com.codesmyth.droidcook.common.util;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

public class Device {

  private Context mContext;

  public Device(Context context) {
    mContext = context;
  }

  public WindowManager windowManager() {
    return (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
  }

  public DisplayMetrics displayMetrics() {
    DisplayMetrics metrics = new DisplayMetrics();
    windowManager().getDefaultDisplay().getMetrics(metrics);
    return metrics;
  }

  public Point displaySize() {
    Display display = windowManager().getDefaultDisplay();
    Point size = new Point();
    if (Build.VERSION.SDK_INT >= 13) {
      display.getSize(size);
    } else {
      size.x = display.getWidth();
      size.y = display.getHeight();
    }
    return size;
  }

  public int dipValue(float x) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, displayMetrics());
  }

  public int dipValue2(float x) {
    return Math.round(x * displayMetrics().density + 0.5f);
  }
}
