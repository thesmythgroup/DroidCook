package com.codesmyth.droidcook.test;

import com.codesmyth.droidcook.api.Bundler;

public interface Wrapper {

  @Bundler
  interface Zero {
    int id();
  }

  @Bundler
  interface One {
    int id();
  }
}
