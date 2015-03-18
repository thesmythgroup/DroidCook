package com.codesmyth.droidcook.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class DetailsVisitor extends ClassVisitor implements Opcodes {

	private Details mDetails;
	private ClassVisitor mVisitor;

	// reference to class/field/method to watch for annotations
	private String mWatching;

	private DetailsVisitor() {
		super(ASM4);
		mDetails = new Details();
	}

	public static Details getDetails(byte[] cls) {
		ClassReader cr = new ClassReader(cls);
		DetailsVisitor dv = new DetailsVisitor();
		cr.accept(dv, 0);
		return dv.mDetails;
	}

	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		mDetails.setClassName(name);
		mDetails.setSuperName(superName);
		checkAnnotation(name);
	}

	public void visitSource(String source, String debug) { }

	public void visitOuterClass(String owner, String name, String desc) {
		// checkAnnotation(name);
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		BaseAnnotationVisitor as = new BaseAnnotationVisitor();
		mDetails.getAnnotations().putInTemp(desc, as);
		return as;
	}

	public void visitAttribute(Attribute attr) { }

	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		// TODO check name against annotation name to prevent false positive
		// checkAnnotation(name);
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		mDetails.setFieldArgs(name, desc, signature);
		checkAnnotation(name);
		return new BaseFieldVisitor(mDetails.getAnnotations());
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		mDetails.setMethodArgs(name, desc, signature);
		checkAnnotation(name);
		return new BaseMethodVisitor(mDetails.getAnnotations());
	}

	public void visitEnd() {
		checkAnnotation(null);
	}

	public void checkAnnotation(String name) {
		if (mWatching == null) {
			if (name == null) {
				return;
			}
			mWatching = name;
		}

		if (!mDetails.getAnnotations().isTempEmpty()) {
			mDetails.getAnnotations().registerWithTemp(mWatching);

			// check if @NoTransform is set on class
			if (mWatching.equals(mDetails.getClassName())) {
				boolean b = mDetails.getAnnotations().contains(mWatching, "Lorg/tsg/android/api/Annotations$NoTransform;");
				mDetails.setNoTransform(b);
			}
		}

		mWatching = name;
	}
}
