package com.codesmyth.android.common.content;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class Receiver extends BroadcastReceiver {

  private Receiver() {}

  public static Receiver create() {
    return new Receiver();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE})
  public static @interface Action {
    String value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  public static @interface Key {
    String value();
  }

  public static interface IEvent {
    void broadcast(Context context);
  }

  private static class P {
    Method m;
    Class  c;
  }

  private Map<String, P> mEvents = new HashMap<>();
  private Object mObj;

  @SuppressWarnings("unchecked")
  public static <T extends IEvent> T create(Bundle extras, Class<T> ev) {
    return (T) Proxy.newProxyInstance(ev.getClassLoader(), new Class<?>[]{ev}, new EventHandler(getAction(ev), extras));
  }

  public static <T extends IEvent> T create(Class<T> ev) {
    return create(new Bundle(), ev);
  }

  public void register(Activity act, Object obj) {
    if (mObj != null) {
      throw new RuntimeException("Already have registered object.");
    }
    mObj = obj;
    for (Method m : obj.getClass().getMethods()) {
      Class<?>[] params = m.getParameterTypes();
      if (params.length == 1 && IEvent.class.isAssignableFrom(params[0])) {
        String action = getAction(params[0]);
        P p = new P();
        p.m = m;
        p.c = params[0];
        mEvents.put(action, p);
      }
    }

    IntentFilter filter = new IntentFilter();
    for (String action : mEvents.keySet()) {
      filter.addAction(action);
    }
    act.registerReceiver(this, filter);
  }

  public void unregister(Activity act) {
    act.unregisterReceiver(this);
    mObj = null;
    mEvents.clear();
  }

  public static String getAction(Class cls) {
    String action;
    if (cls.isAnnotationPresent(Action.class)) {
      action = ((Action) cls.getAnnotation(Action.class)).value();
    } else {
      action = cls.getName();
    }
    return action;
  }

  public static String getKey(Method m) {
    String key;
    if (m.isAnnotationPresent(Key.class)) {
      key = m.getAnnotation(Key.class).value();
    } else {
      key = m.getName();
    }
    return key;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (mEvents.containsKey(intent.getAction())) {
      P p = mEvents.get(intent.getAction());
      try {
        p.m.invoke(mObj, create(intent.getExtras(), p.c));
      } catch (IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }

  private static Map<String, Class> mPrimitives = new HashMap<>();

  static {
    mPrimitives.put("Boolean", boolean.class);
    mPrimitives.put("Byte", byte.class);
    mPrimitives.put("Char", char.class);
    mPrimitives.put("Integer", int.class);
    mPrimitives.put("Short", short.class);
    mPrimitives.put("Float", float.class);
    mPrimitives.put("Double", double.class);
    mPrimitives.put("Long", long.class);
  }

  static class EventHandler implements InvocationHandler {
    private String mAction;
    private Bundle mExtras;

    public EventHandler(String action, Bundle extras) {
      mAction = action;
      mExtras = extras;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if ("broadcast".equals(method.getName())) {
        ((Context) args[0]).sendBroadcast(new Intent(mAction).putExtras(mExtras));
        return null;
      } else if (args.length == 0) {
        return mExtras.get(getKey(method));
      } else if (args.length == 1) {
        Class value = args[0].getClass();
        String name = value.getSimpleName();

        if (mPrimitives.containsKey(name)) {
          value = mPrimitives.get(name);
        }

        Method put = mExtras.getClass().getMethod("put" + name, String.class, value);
        put.invoke(mExtras, getKey(method), args[0]);
        return proxy;
      }
      throw new RuntimeException("Unsupported argument length " + args.length);
    }
  }
}