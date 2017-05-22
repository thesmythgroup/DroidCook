package com.codesmyth.droidcook.test;

import com.codesmyth.droidcook.api.Event;

public interface Wrapper {

  @Event
  interface Zero {
    int id();
  }

  @Event
  interface One {
    int id();
  }
}
