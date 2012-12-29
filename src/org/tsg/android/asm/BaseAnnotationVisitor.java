package org.tsg.android.asm;

import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

public class BaseAnnotationVisitor extends AnnotationVisitor implements Opcodes {

	private AnnotationVisitor mVisitor;
	private Map<String, Object> mMap;

	public BaseAnnotationVisitor(AnnotationVisitor av) {
		super(ASM4, av);
		mVisitor = av;
		mMap = new HashMap<String, Object>();
	}

	public void visit(String name, Object value) {
		mMap.put(name, value);
		mVisitor.visit(name, value);
	}

	public void visitEnd() {
		mVisitor.visitEnd();
	}

	public Object get(String key) {
		return mMap.get(key);
	}
}
