package com.codesmyth.droidcook.common.widget;

import java.util.concurrent.atomic.AtomicInteger;

@Deprecated
public class LoaderId {

  private static final AtomicInteger mLast = new AtomicInteger(0);

  public static int next() {
    return mLast.incrementAndGet();
  }
}
