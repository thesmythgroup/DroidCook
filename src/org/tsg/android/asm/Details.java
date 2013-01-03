package org.tsg.android.asm;

import java.lang.reflect.Field;
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

	private Class mR;

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

	public String getMethodDesc(String name) {
		if (!mMethods.containsKey(name)) {
			return null;
		}
		return mMethods.get(name).mDesc;
	}

	public String getMethodSignature(String name) {
		return mMethods.get(name).mSignature;
	}

	public void setFieldArgs(String name, String desc, String signature) {
		mFields.put(name, new Description(desc, signature));
	}

	public void setMethodArgs(String name, String desc, String signature) {
		mMethods.put(name, new Description(desc, signature));
	}

	public void setR(Class r) {
		mR = r;
	}

	public int getResourceId(String name) {
		Class cls = getResourceClass("R$id");
		try {
			Field f = cls.getField(name);
			return (Integer) f.get(null);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Couldn't locate R.id." + name + " in reference to " + mClassName);
	}

	public int getResourceLayout(String name) {
		Class cls = getResourceClass("R$layout");
		try {
			Field f = cls.getField(name);
			return (Integer) f.get(null);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Couldn't locate R.layout." + name + " in reference to " + mClassName);
	}

	private Class getResourceClass(String name) {
		for (Class cls : mR.getDeclaredClasses()) {
			if (cls.getName().endsWith(name)) {
				return cls;
			}
		}
		throw new RuntimeException("Couldn't locate " + name);
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
