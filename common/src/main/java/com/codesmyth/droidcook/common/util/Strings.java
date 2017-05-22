package com.codesmyth.droidcook.common.util;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

public final class Strings {

  public static boolean ok(String s) {
    return !isNullOrEmpty(s);
  }

  public static boolean isNullOrEmpty(String s) {
    return "".equals(of(s).trim());
  }

  public static String of(String s) {
    return nullToEmpty(s);
  }

  public static String nullToEmpty(String s) {
    if (s == null) {
      return "";
    }
    return s;
  }

  public static String read(Reader r) throws IOException {
    final int nbuf = 1024;
    char[] a = new char[nbuf];
    int i = 0;
    for (int n = 0; n != -1; n = r.read(a, i, a.length - i)) {
      if ((i += n) == a.length) {
        a = Arrays.copyOf(a, a.length + nbuf);
      }
    }
    return new String(a, 0, i);
  }
}
