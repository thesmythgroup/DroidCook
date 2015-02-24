package com.codesmyth.android.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

public class BaseFieldVisitor extends FieldVisitor implements Opcodes {

	private FieldVisitor mVisitor;
	private Annotations mAnnotations;

	public BaseFieldVisitor(Annotations annotations) {
		super(ASM4);
		mAnnotations = annotations;
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		BaseAnnotationVisitor as = new BaseAnnotationVisitor();
		mAnnotations.putInTemp(desc, as);
		return as;
	}
}
