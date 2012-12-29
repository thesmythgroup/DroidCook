package org.tsg.android.asm;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;

public class Details {

	private Annotations mAnnotations;
	private Map<String, String> mFields;
	private Map<String, String> mMethods;

	private String mClassName;
	private String mSuperName;
	private boolean mNoTransform;

	public Details() {
		mAnnotations = new Annotations();
		mFields = new HashMap<String, String>();
		mMethods = new HashMap<String, String>();
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
		return mFields.get(name);
	}

	public String getFieldType(String name) {
		String desc = mFields.get(name);
		if (desc == null) {
			return null;
		}
		return desc.substring(1, (desc.length() - 1));
	}

	public void setFieldArgs(String name, String desc) {
		mFields.put(name, desc);
	}

	public void setMethodArgs(String name, String desc) {
		mMethods.put(name, desc);
	}

	public String toString() {
		String s = mClassName + " extends " + mSuperName + "\n";
		for (Entry<String, String> entry : mFields.entrySet()) {
			s += "\n";
			if (mAnnotations.containsKey(entry.getKey())) {
				for (String annotation : mAnnotations.getAll(entry.getKey()).keySet()) {
					s += "  @" + annotation + "\n";
				}
			}
			s += "  field " + entry.getKey() + " " + entry.getValue() + "\n";
		}
		for (Entry<String, String> entry : mMethods.entrySet()) {
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
}
