package com.codesmyth.droidcook.common.content;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public abstract class ManagedEvent<A, B, C> {

  private static final String TAG = "ManagedEvent";

  private HashMap<A, Boolean> evs = new HashMap<>();
  private Activity act = null;

  protected abstract void registerEvent(Activity a, A e);

  protected abstract void unregisterEvent(Activity a, A e);

  protected abstract A makeEvent(C c, B b);

  public void purgeEvents() {
    unregisterAll();
    evs.clear();
    act = null;
  }

  public void registerEvents(Activity a) {
    unregisterAll();
    act = a;
    registerAll();
  }

  public void unregisterEvents() {
    if (act != null) {
      unregisterAll();
    }
  }

  @SafeVarargs
  public final void register(A... events) {
    for (A e : events) {
      if (!evs.containsKey(e)) {
        evs.put(e, false);
      }
      if (act != null) {
        registerEvent(act, e);
        evs.put(e, true);
      }
    }
  }

  @SafeVarargs
  public final void unregister(A... events) {
    for (A e : events) {
      if (!evs.containsKey(e)) {
        Log.d(TAG, "Attempting to unregister event not managed.");
      }
      if (act != null) {
        try {
          unregisterEvent(act, e);
        } catch (IllegalArgumentException t) {
          t.printStackTrace();
          Log.e(TAG, "unregister event failed: " + String.valueOf(e));
        }
      }
      evs.remove(e);
    }
  }

  public void event(C c, B b) {
    register(makeEvent(c, b));
  }

  private void unregisterAll() {
    if (act == null) {
      return;
    }
    for (Map.Entry<A, Boolean> entry : evs.entrySet()) {
      if (!entry.getValue()) {
        continue;
      }
      evs.put(entry.getKey(), false);
      try {
        unregisterEvent(act, entry.getKey());
      } catch (IllegalArgumentException t) {
        Log.e(TAG, "unregister failed", t);
      }
    }
  }

  private void registerAll() {
    if (act == null) {
      return;
    }
    for (Map.Entry<A, Boolean> entry : evs.entrySet()) {
      if (entry.getValue()) {
        continue;
      }
      try {
        registerEvent(act, entry.getKey());
      } catch (IllegalArgumentException t) {
        Log.e(TAG, "register failed", t);
      }
    }
  }

  public interface ReceiverFunc {

    void apply(PackedReceiver pr, Intent intent);
  }

  public interface ObserverFunc {

    void apply(PackedObserver po, Boolean b);
  }

  public static abstract class PackedReceiver extends BroadcastReceiver {

    public abstract IntentFilter filter();
  }

  public static abstract class PackedObserver extends ContentObserver {

    public PackedObserver() {
      super(new Handler());
    }

    public abstract Uri uri();
  }

  public static abstract class ReceiverEvent extends
      ManagedEvent<PackedReceiver, ReceiverFunc, String> {

    @Override
    protected PackedReceiver makeEvent(final String s, final ReceiverFunc receiverFunc) {
      return new PackedReceiver() {
        @Override
        public IntentFilter filter() {
          return new IntentFilter(s);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
          receiverFunc.apply(this, intent);
        }
      };
    }

    @Override
    protected void registerEvent(Activity a, PackedReceiver e) {
      a.registerReceiver(e, e.filter());
    }

    @Override
    protected void unregisterEvent(Activity a, PackedReceiver e) {
      a.unregisterReceiver(e);
    }
  }

  public static abstract class ObserverEvent extends
      ManagedEvent<PackedObserver, ObserverFunc, Uri> {

    @Override
    protected PackedObserver makeEvent(final Uri uri, final ObserverFunc observerFunc) {
      return new PackedObserver() {
        @Override
        public Uri uri() {
          return uri;
        }

        @Override
        public void onChange(boolean selfChange) {
          observerFunc.apply(this, selfChange);
        }
      };
    }

    @Override
    protected void registerEvent(Activity a, PackedObserver e) {
      a.getContentResolver().registerContentObserver(e.uri(), false, e);
    }

    @Override
    protected void unregisterEvent(Activity a, PackedObserver e) {
      a.getContentResolver().unregisterContentObserver(e);
    }
  }
}
