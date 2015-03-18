package com.codesmyth.droidcook.common.content;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public abstract class PackedReceiver extends BroadcastReceiver {
  public abstract IntentFilter filter();
}
