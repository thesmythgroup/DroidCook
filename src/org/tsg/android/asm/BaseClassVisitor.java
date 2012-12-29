package org.tsg.android.asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public abstract class BaseClassVisitor extends ClassVisitor implements Opcodes {

	protected ClassVisitor mVisitor;
	protected Details mDetails;

	public BaseClassVisitor(ClassVisitor cv, Details details) {
		super(ASM4, cv);
		mVisitor = cv;
		mDetails = details;
	}

	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		mVisitor.visit(version, access, name, signature, superName, interfaces);
	}

	public void visitSource(String source, String debug) {
		mVisitor.visitSource(source, debug);
	}

	public void visitOuterClass(String owner, String name, String desc) {
		mVisitor.visitOuterClass(owner, name, desc);
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return mVisitor.visitAnnotation(desc, visible);
	}

	public void visitAttribute(Attribute attr) {
		mVisitor.visitAttribute(attr);
	}

	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		mVisitor.visitInnerClass(name, outerName, innerName, access);
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		return mVisitor.visitField(access, name, desc, signature, value);
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		return mVisitor.visitMethod(access, name, desc, signature, exceptions);
	}

	public void visitEnd() {
		mVisitor.visitEnd();
	}
}
