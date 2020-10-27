package com.codesmyth.droidcook.common;

@SuppressWarnings("unused")
public final class Strings {

  /**
   * Returns true if String is not null or "".
   * Whitespace is considered a value.
   */
  public static boolean hasValue(String s) {
    return !"".equals(nullToEmpty(s));
  }

  /**
   * Returns "" if String is null, otherwise return the String.
   */
  public static String nullToEmpty(String s) {
    if (s == null) {
      return "";
    }
    return s;
  }

// TODO
//  public static String join(Iterable<? extends CharSequence> a, String sep) {
//  }
}
