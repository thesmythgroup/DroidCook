package org.tsg.android.asm;

import java.util.Map;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class BaseMethodVisitor extends MethodVisitor implements Opcodes {

	private MethodVisitor mVisitor;
	private Map<String, BaseAnnotationVisitor> mAnnotations;

	public BaseMethodVisitor(MethodVisitor mv, Map<String, BaseAnnotationVisitor> annotations) {
		super(ASM4, mv);
		mVisitor = mv;
		mAnnotations = annotations;
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		System.out.println("### visitAnnotation Method " + desc);
		AnnotationVisitor av = mVisitor.visitAnnotation(desc, visible);
		BaseAnnotationVisitor as = new BaseAnnotationVisitor(av);
		mAnnotations.put(desc, as);
		return as;
	}

	public void visitEnd() {
		mVisitor.visitEnd();
	}
}
