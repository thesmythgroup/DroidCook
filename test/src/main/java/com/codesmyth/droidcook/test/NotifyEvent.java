package com.codesmyth.droidcook.test;

import com.codesmyth.droidcook.api.Event;

@Event
public abstract class NotifyEvent {

  abstract long id();
  abstract String message();

  public static Event_NotifyEvent.Builder build() {
    return Event_NotifyEvent.build();
  }
}
