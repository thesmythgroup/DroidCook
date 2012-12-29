package org.tsg.android.asm;

import java.util.Map;
import org.objectweb.asm.ClassVisitor;

public interface Handler {
	boolean match(String name);
	void handle(ClassVisitor cv, String className, String superName, String methodName, Map<String, BaseAnnotationVisitor> annotations);
}
