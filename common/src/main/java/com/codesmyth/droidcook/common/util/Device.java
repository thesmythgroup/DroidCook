package com.codesmyth.droidcook.common.util;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

public class Device {
  public static WindowManager windowManager(Context context) {
    return (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
  }

  public static DisplayMetrics displayMetrics(Context context) {
    DisplayMetrics metrics = new DisplayMetrics();
    windowManager(context).getDefaultDisplay().getMetrics(metrics);
    return metrics;
  }

  public static Point displaySize(Context context) {
    Display display = windowManager(context).getDefaultDisplay();
    Point size = new Point();
    if (Build.VERSION.SDK_INT >= 13) {
      display.getSize(size);
    } else {
      size.x = display.getWidth();
      size.y = display.getHeight();
    }
    return size;
  }

  public static int dipValue(Context context, float x) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, displayMetrics(context));
  }

  public static int dipValue2(Context context, float x) {
    return Math.round(x * displayMetrics(context).density + 0.5f);
  }
}
