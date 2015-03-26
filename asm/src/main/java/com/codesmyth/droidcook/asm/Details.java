package com.codesmyth.droidcook.asm;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;

public class Details {

	private enum Resource {
		ID("R$id"),
		LAYOUT("R$layout"),
		ANIMATION("R$anim"),
		INTEGER("R$integer"),
		BOOLEAN("R$bool"),
		COLOR("R$color"),
		STRING("R$string"),
		ARRAY("R$array"),
		DRAWABLE("R$drawable");

		private String mClassName;

		private Resource(String className) {
			mClassName = className;
		}

		public String getClassName() {
			return mClassName;
		}
	}

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

	private Class getResourceClass(String name) {
		for (Class cls : mR.getDeclaredClasses()) {
			if (cls.getName().endsWith(name)) {
				return cls;
			}
		}
		throw new RuntimeException("Couldn't locate " + name);
	}

	private int getResource(String name, Resource res) {
		String className = res.getClassName();
		Class cls = getResourceClass(className);
		try {
			Field f = cls.getField(name);
			return (Integer) f.get(null);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Couldn't locate " + className.replace("\\$", "\\.") + "." + name + " in reference to " + mClassName);
	}

	public int getResourceId(String name) {
		return getResource(name, Resource.ID);
	}

	public int getResourceLayout(String name) {
		return getResource(name, Resource.LAYOUT);
	}

	public int getResourceAnimation(String name) {
		return getResource(name, Resource.ANIMATION);
	}

	public int getResourceColor(String name) {
		return getResource(name, Resource.COLOR);
	}

	public int getResourceInteger(String name) {
		return getResource(name, Resource.INTEGER);
	}

	public int getResourceBoolean(String name) {
		return getResource(name, Resource.BOOLEAN);
	}

	public int getResourceString(String name) {
		return getResource(name, Resource.STRING);
	}

	public int getResourceArray(String name) {
		return getResource(name, Resource.ARRAY);
	}

	public int getResourceDrawable(String name) {
		return getResource(name, Resource.DRAWABLE);
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
