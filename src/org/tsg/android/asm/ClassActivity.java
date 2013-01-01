package org.tsg.android.asm;

import org.objectweb.asm.Label;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassActivity extends ClassVisitor implements Opcodes {

	private enum Override {
		ONCREATE;

		public static Override from(String name) {
			try {
				return Override.valueOf(name.toUpperCase());
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}

	private List<Override> mOverridden;
	private ClassVisitor mVisitor;
	private Details mDetails;
	private File mFile;

	public ClassActivity(ClassVisitor cv, Details details, File file) {
		super(ASM4, cv);
		mVisitor = cv;
		mDetails = details;
		mFile = file;
		mOverridden = new ArrayList<Override>();
	}

	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		mVisitor.visit(version, access, name, signature, superName, interfaces);

		Annotations ann = mDetails.getAnnotations();
		if (ann.exists(Annotations.ON_CLICK)) {
			String anonName = Utils.nextInnerClassName(mFile, mDetails.getClassName());
			for (String method : ann.namesFor(Annotations.ON_CLICK)) {
				int[] ids = (int[]) ann.get(method, Annotations.ON_CLICK, "value");
				for (int id : ids) {
					mVisitor.visitInnerClass(anonName, null, null, 0);
					anonName = Utils.nextInnerClassName(anonName);
				}
			}
			mVisitor.visitInnerClass("android/view/View$OnClickListener", "android/view/View", "OnClickListener", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE);
		}
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = null;
		Override override = Override.from(name);
		if (override != null) {
			mOverridden.add(override);
			// TODO strip calls to super
			mv = mVisitor.visitMethod(access, "_" + name, desc, signature, exceptions);
		} else {
			mv = mVisitor.visitMethod(access, name, desc, signature, exceptions);
		}

		return mv;
	}

	public void visitEnd() {
		onCreate();
		mVisitor.visitEnd();
	}

	public void onCreate() {

		Annotations ann = mDetails.getAnnotations();
		String className = mDetails.getClassName();
		String superName = mDetails.getSuperName();
		boolean overridden = mOverridden.contains(Override.ONCREATE);

		Maxs maxs = new Maxs();
		maxs.setLocals(2);

		MethodVisitor mv = mVisitor.visitMethod(ACC_PUBLIC, "onCreate", "(Landroid/os/Bundle;)V", null, null);
		mv.visitCode();

		Methods.onCreateSuper(mv, maxs, mDetails);

		if (ann.contains(className, Annotations.CONTENT_VIEW)) {
			Integer id = (Integer) ann.get(className, Annotations.CONTENT_VIEW, "value");
			Methods.setContentView(mv, maxs, mDetails, id);
		}

		if (ann.exists(Annotations.VIEW_BY_ID)) {
			for (String field : ann.namesFor(Annotations.VIEW_BY_ID)) {
				Integer id = (Integer) ann.get(field, Annotations.VIEW_BY_ID, "value");
				Methods.findViewById(mv, maxs, mDetails, field, id);
			}
		}

		if (overridden) {
			Methods.onCreateVirtual(mv, maxs, mDetails, "_onCreate");
		}

		if (ann.exists(Annotations.ON_CREATE)) {
			for (String method : ann.namesFor(Annotations.ON_CREATE)) {
				Methods.onCreateVirtual(mv, maxs, mDetails, method);
			}
		}

		if (ann.exists(Annotations.ON_CLICK)) {
			String anonName = Utils.nextInnerClassName(mFile, mDetails.getClassName());
			for (String method : ann.namesFor(Annotations.ON_CLICK)) {
				int[] ids = (int[]) ann.get(method, Annotations.ON_CLICK, "value");
				for (int id : ids) {
					Methods.setOnClickListener(mv, maxs, mDetails, anonName, id);
					Utils.newAnonymousInnerOnClick(mDetails.getClassName(), anonName, method, mFile);
					anonName = Utils.nextInnerClassName(anonName);
				}
			}
		}

		//
		Methods.onCreateReturn(mv, maxs);
		mv.visitEnd();
	}

	/**
	 * 
	 */
	public void onClick2(MethodVisitor mv, Maxs maxs, int[] ids) {
		String className = mDetails.getClassName();
		String anonName = className + "$1";

		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitTypeInsn(NEW, anonName);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, anonName, "<init>", "(L" + className + ";)V");
		mv.visitVarInsn(ASTORE, 1);
		Label l1 = new Label();
		mv.visitLabel(l1);
		for (int id : ids) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitLdcInsn(new Integer(id));
			mv.visitMethodInsn(INVOKEVIRTUAL, className, "findViewById", "(I)Landroid/view/View;");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "android/view/View", "setOnClickListener", "(Landroid/view/View$OnClickListener;)V");
		}

		maxs.setStack(3);

		//TODO move to onClickReturn() but account for Labels below
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitInsn(RETURN);
		Label l3 = new Label();
		mv.visitLabel(l3);
		// TODO one for each call to method, defer somehow
		mv.visitLocalVariable("l", "Landroid/view/View$OnClickListener;", null, l1, l2, 1);
		//
		mv.visitLocalVariable("this", "L" + className + ";", null, l0, l3, 0);
	}

}
