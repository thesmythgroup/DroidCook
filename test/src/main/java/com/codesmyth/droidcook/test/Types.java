package com.codesmyth.droidcook.test;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import com.codesmyth.droidcook.api.Event;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("ALL")
@Event
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

	Uri valueUri();
}
