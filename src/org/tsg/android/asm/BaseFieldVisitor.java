package org.tsg.android.asm;

import java.util.Map;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

public class BaseFieldVisitor extends FieldVisitor implements Opcodes {

	private FieldVisitor mVisitor;
	private Map<String, BaseAnnotationVisitor> mAnnotations;

	public BaseFieldVisitor(FieldVisitor fv, Map<String, BaseAnnotationVisitor> annotations) {
		super(ASM4, fv);
		mVisitor = fv;
		mAnnotations = annotations;
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		System.out.println("### visitAnnotation Field " + desc);
		AnnotationVisitor av = mVisitor.visitAnnotation(desc, visible);
		BaseAnnotationVisitor as = new BaseAnnotationVisitor(av);
		mAnnotations.put(desc, as);
		return as;
	}

	public void visitAttribute(Attribute attr) {
		mVisitor.visitAttribute(attr);
	}

	public void visitEnd() {
		mVisitor.visitEnd();
	}
}
