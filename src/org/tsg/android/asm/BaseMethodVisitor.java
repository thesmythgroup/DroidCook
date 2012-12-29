package org.tsg.android.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class BaseMethodVisitor extends MethodVisitor implements Opcodes {

	private MethodVisitor mVisitor;
	private Annotations mAnnotations;

	public BaseMethodVisitor(Annotations annotations) {
		super(ASM4);
		mAnnotations = annotations;
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		BaseAnnotationVisitor as = new BaseAnnotationVisitor();
		mAnnotations.putInTemp(desc, as);
		return as;
	}
}
