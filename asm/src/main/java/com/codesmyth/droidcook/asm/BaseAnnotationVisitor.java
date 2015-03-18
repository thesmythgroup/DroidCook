package com.codesmyth.droidcook.asm;

import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

public class BaseAnnotationVisitor extends AnnotationVisitor implements Opcodes {

	private Map<String, Object> mMap;

	public BaseAnnotationVisitor() {
		super(ASM4);
		mMap = new HashMap<String, Object>();
	}

	public void visit(String name, Object value) {
		mMap.put(name, value);
	}

	public Object get(String key) {
		return mMap.get(key);
	}
}
