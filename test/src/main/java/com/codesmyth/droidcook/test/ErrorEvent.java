package com.codesmyth.droidcook.test;

import com.codesmyth.droidcook.api.Event;

@Event
public interface ErrorEvent {
  long id();
  String message();
  int count();
  boolean done();
}
