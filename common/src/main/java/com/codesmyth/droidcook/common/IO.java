package com.codesmyth.droidcook.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

@SuppressWarnings("unused")
public class IO {
  /**
   * default buffer size for IO methods
   */
  static final int nbuf = 8192;

  /**
   * Reads from src one byte at a time until the first occurrence of delim, but
   * will return early if no data available; this call will not block on src
   * and is suitable for use with long-standing connections such as with Socket.
   *
   * The delimiter is included in the result if encountered.
   */
  public static byte[] readBytes(InputStream src, char delim) throws IOException {
    byte[] buf = new byte[nbuf];
    int c, i = 0;
    while (src.available() > 0 && (c = src.read()) > 0) {
      if (i == nbuf) {
        buf = Arrays.copyOf(buf, buf.length + nbuf);
      }
      buf[i] = (byte) c;
      i++;
      if (c == delim) {
        break;
      }
    }
    return Arrays.copyOf(buf, i);
  }

  /**
   * Helper for returning new String of readBytes output. Consult readBytes for the
   * various return cases.
   */
  public static String readString(InputStream src, char delim) throws IOException {
    return new String(readBytes(src, delim));
  }

  /**
   * Reads from src in chunks until EOF or src is closed. This call will block
   * until data becomes available.
   */
  public static byte[] readAll(InputStream src) throws IOException {
    byte[] buf = new byte[nbuf];
    int n, i = 0;
    while ((n = src.read(buf, i, buf.length - i)) != -1) {
      if ((i += n) == buf.length) {
        buf = Arrays.copyOf(buf, buf.length + nbuf);
      }
    }
    return Arrays.copyOf(buf, i);
  }

  /**
   * Reads from src in chunks until EOF or src is closed. This call will block
   * until data becomes available. Each successful read is immediately written
   * to dst.
   */
  public static void copy(OutputStream dst, InputStream src) throws IOException {
    byte[] buf = new byte[nbuf];
    int n;
    while ((n = src.read(buf)) != -1) {
      dst.write(buf, 0, n);
    }
  }
}