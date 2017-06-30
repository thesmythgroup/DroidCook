package com.codesmyth.droidcook.test;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import com.codesmyth.droidcook.api.Bundler;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Lists all types checked. Do not reference this as documentation for what types are supported.
 *
 * Types commented out were known not to work when checked, but may work now.
 * Exclusion of a type does not mean it's not supported.
 */
@SuppressWarnings("ALL")
@Bundler
public interface Types {
  // IBinder valueBinder();
  boolean valueBoolean();
  boolean[] valueBooleanArray();
  Bundle valueBundle();
  byte valueByte();
  byte[] valueByteArray();
  char valueChar();
  char[] valueCharArray();
  CharSequence valueCharSequence();
  CharSequence[] valueCharSequenceArray();
  ArrayList<CharSequence> valueCharSequenceArrayList();
  double valueDouble();
  double[] valueDoubleArray();
  float valueFloat();
  float[] valueFloatArray();
  int valueInt();
  int[] valueIntArray();
  ArrayList<Integer> valueIntegerArrayList();
  long valueLong();
  long[] valueLongArray();
  Parcelable valueParcelable();
  Parcelable[] valueParcelableArray();
  // ArrayList<? extends Parcelable> valueParcelableArrayList();
  Serializable valueSerializable();
  short valueShort();
  short[] valueShortArray();
  // Size valueSize();
  // SizeF valueSizeF();
  // SparseArray<? extends Parcelable> valueSparseParcelableArray();
  String valueString();
  String[] valueStringArray();
  ArrayList<String> valueStringArrayList();

  // or any type that implements Parceable.
  Uri valueUri();
  //Uri[] valueUris();
}
