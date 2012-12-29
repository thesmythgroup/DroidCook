package org.tsg.android.asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Annotations {

	private Set<String> mNames;
	private Map<String, Map<String, BaseAnnotationVisitor>> mMap;
	private Map<String, BaseAnnotationVisitor> mTemp;

	public Annotations() {
		mNames = new HashSet<String>();
		mTemp = new HashMap<String, BaseAnnotationVisitor>();
		mMap = new HashMap<String, Map<String, BaseAnnotationVisitor>>();
	}

	public boolean isTempEmpty() {
		return mTemp.isEmpty();
	}

	public void putInTemp(String name, BaseAnnotationVisitor bav) {
		mNames.add(name);
		mTemp.put(name, bav);
	}

	public void registerWithTemp(String name) {
		HashMap<String, BaseAnnotationVisitor> map = new HashMap<String, BaseAnnotationVisitor>();
		map.putAll(mTemp);
		mMap.put(name, map);
		mTemp.clear();
	}

	public Object get(String name, String annotation, String key) {
		if (!mMap.containsKey(name)) {
			return null;
		}
		BaseAnnotationVisitor bav = mMap.get(name).get(annotation);
		if (bav == null) {
			return null;
		}
		return bav.get(key);
	}

	public Map<String, BaseAnnotationVisitor> getAll(String name) {
		return mMap.get(name);
	}

	public boolean contains(String name, String annotation) {
		if (mMap.containsKey(name) && mMap.get(name).containsKey(annotation)) {
			return true;
		}
		return false;
	}

	public boolean containsKey(String name) {
		return mMap.containsKey(name);
	}

	public boolean exists(String annotation) {
		return mNames.contains(annotation);
	}

	public List<String> namesFor(String annotation) {
		List<String> names = new ArrayList<String>();
		for (String name : mMap.keySet()) {
			if (contains(name, annotation)) {
				names.add(name);
			}
		}
		return names;
	}
}
