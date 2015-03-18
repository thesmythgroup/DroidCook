package com.codesmyth.droidcook.common.util;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Logger {

  private String mTag;
  private int    mLevel;

  public Logger(String tag, int level) {
    mTag = tag;
    mLevel = level;
  }

  public Logger(Object obj, int level) {
    this(obj.getClass().getName(), level);
  }

  public Logger(Object obj) {
    this(obj, Log.DEBUG);
  }

  public Logger(String tag) {
    this(tag, Log.DEBUG);
  }

  private boolean isLoggable(int lvl) {
    return lvl >= mLevel;
  }

  private String makeMessage(Object[] objs) {
    String message = "";
    for (Object obj : objs) {
      message += String.valueOf(obj) + " ";
    }
    return message;
  }

  public int d(Object... objs) {
    if (isLoggable(Log.DEBUG)) {
      return Log.d(mTag, makeMessage(objs));
    } else {
      return -1;
    }
  }

  public int e(Object... objs) {
    if (isLoggable(Log.ERROR)) {
      return Log.e(mTag, makeMessage(objs));
    } else {
      return -1;
    }
  }

  public int i(Object... objs) {
    if (isLoggable(Log.INFO)) {
      return Log.i(mTag, makeMessage(objs));
    } else {
      return -1;
    }
  }

  public int w(Object... objs) {
    if (isLoggable(Log.WARN)) {
      return Log.w(mTag, makeMessage(objs));
    } else {
      return -1;
    }
  }

  public int v(Object... objs) {
    if (isLoggable(Log.VERBOSE)) {
      return Log.v(mTag, makeMessage(objs));
    } else {
      return -1;
    }
  }

  public int wtf(Object... objs) {
    try {
      Method m = Log.class.getMethod("wtf", String.class, String.class);
      return (Integer) m.invoke(null, mTag, makeMessage(objs));
    } catch (IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      w("Log.wtf method not supported.");
    }
    return Log.e(mTag, makeMessage(objs));
  }
}
