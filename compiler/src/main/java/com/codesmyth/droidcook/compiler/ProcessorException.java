package com.codesmyth.droidcook.compiler;

class ProcessorException extends Exception {
  public ProcessorException(String msg) {
    super(msg);
  }

  public ProcessorException(Exception e) {
    super(e);
  }
}