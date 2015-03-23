package com.codesmyth.droidcook.test;

import com.codesmyth.droidcook.api.Event;

@Event
public abstract class NotifyEvent {

  public abstract long id();
  public abstract String message();

  public static Event_NotifyEvent.Builder build() {
    return Event_NotifyEvent.build();
  }
}
