package com.codesmyth.droidcook.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

@SuppressWarnings("unused")
public final class Fmt {
  /**
   * Writes String to dst.
   */
  public static void fprint(OutputStream dst, String src) throws IOException {
    IO.copy(dst, new ByteArrayInputStream(Strings.nullToEmpty(src).getBytes("UTF-8")));
  }

  /**
   * Writes formatted String via String.format to dst.
   */
  public static void fprintf(OutputStream dst, String format, Object... objects)
      throws IOException {
    fprint(dst, String.format(format, objects));
  }

  /**
   * Writes formatted String via String.format and newline char to dst.
   */
  public static void fprintln(OutputStream dst, String src) throws IOException {
    fprint(dst, src);
    dst.write('\n');
  }
}
