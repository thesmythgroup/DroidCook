package com.codesmyth.droidcook.test;

import com.codesmyth.droidcook.api.Bundler;

@Bundler
public interface Inform {
  long id();
  String message();

  default String squaredString() {
    return String.valueOf(id()*id());
  }

  static InformBundler bundler() {
    return new InformBundler();
  }
}
