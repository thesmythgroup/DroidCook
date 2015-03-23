package com.codesmyth.droidcook.test;

import android.os.Bundle;
import android.os.Parcelable;
import com.codesmyth.droidcook.api.Event;

import java.io.Serializable;
import java.util.ArrayList;

@Event
public abstract class EverythingEvent {

  //  public abstract IBinder                           valueBinder();
  public abstract boolean valueBoolean();
  public abstract boolean[] valueBooleanArray();
  public abstract Bundle valueBundle();
  public abstract byte valueByte();
  public abstract byte[] valueByteArray();
  public abstract char valueChar();
  public abstract char[] valueCharArray();
  public abstract CharSequence valueCharSequence();
  public abstract CharSequence[] valueCharSequenceArray();
  public abstract ArrayList<CharSequence> valueCharSequenceArrayList();
  public abstract double valueDouble();
  public abstract double[] valueDoubleArray();
  public abstract float valueFloat();
  public abstract float[] valueFloatArray();
  public abstract int valueInt();
  public abstract int[] valueIntArray();
  public abstract ArrayList<Integer> valueIntegerArrayList();
  public abstract long valueLong();
  public abstract long[] valueLongArray();
  public abstract Parcelable valueParcelable();
  public abstract Parcelable[] valueParcelableArray();
  //  public abstract ArrayList<? extends Parcelable>   valueParcelableArrayList();
  public abstract Serializable valueSerializable();
  public abstract short valueShort();
  public abstract short[] valueShortArray();
  //  public abstract Size                              valueSize();
//  public abstract SizeF                             valueSizeF();
//  public abstract SparseArray<? extends Parcelable> valueSparseParcelableArray();
  public abstract String valueString();
  public abstract String[] valueStringArray();
  public abstract ArrayList<String> valueStringArrayList();

  public static Event_EverythingEvent.Builder build() {
    return Event_EverythingEvent.build();
  }
}
