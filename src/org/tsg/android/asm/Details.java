package org.tsg.android.asm;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;

public class Details {

	private Annotations mAnnotations;
	private Map<String, Description> mFields;
	private Map<String, Description> mMethods;

	private String mClassName;
	private String mSuperName;
	private boolean mNoTransform;

	public Details() {
		mAnnotations = new Annotations();
		mFields = new HashMap<String, Description>();
		mMethods = new HashMap<String, Description>();
	}

	public boolean noTransform() {
		return mNoTransform;
	}

	public void setNoTransform(boolean b) {
		mNoTransform = b;
	}

	public String getClassName() {
		return mClassName;
	}

	public void setClassName(String name) {
		mClassName = name;
	}

	public String getSuperName() {
		return mSuperName;
	}

	public void setSuperName(String name) {
		mSuperName = name;
	}

	public Annotations getAnnotations() {
		return mAnnotations;
	}

	public String getFieldDesc(String name) {
		return mFields.get(name).mDesc;
	}

	public String getFieldType(String name) {
		String desc = mFields.get(name).mDesc;
		if (desc == null) {
			return null;
		}
		return desc.substring(1, (desc.length() - 1));
	}

	public void setFieldArgs(String name, String desc, String signature) {
		mFields.put(name, new Description(desc, signature));
	}

	public void setMethodArgs(String name, String desc, String signature) {
		mMethods.put(name, new Description(desc, signature));
	}

	public String toString() {
		String s = mClassName + " extends " + mSuperName + "\n";
		for (Entry<String, Description> entry : mFields.entrySet()) {
			s += "\n";
			if (mAnnotations.containsKey(entry.getKey())) {
				for (String annotation : mAnnotations.getAll(entry.getKey()).keySet()) {
					s += "  @" + annotation + "\n";
				}
			}
			s += "  field " + entry.getKey() + " " + entry.getValue() + "\n";
		}
		for (Entry<String, Description> entry : mMethods.entrySet()) {
			s += "\n";
			if (mAnnotations.containsKey(entry.getKey())) {
				for (String annotation : mAnnotations.getAll(entry.getKey()).keySet()) {
					s += "  @" + annotation + "\n";
				}
			}
			s += "  method " + entry.getKey() + " " + entry.getValue() + "\n";
		}

		return s;
	}

	public static final class Description {

		String mDesc;
		String mSignature;

		public Description(String desc, String signature) {
			mDesc = desc;
			mSignature = signature;
		}

		public String toString() {
			return mDesc;
		}
	}
}
