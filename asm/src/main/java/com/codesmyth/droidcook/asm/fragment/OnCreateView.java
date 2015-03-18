package com.codesmyth.droidcook.asm.fragment;

import java.io.File;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import com.codesmyth.droidcook.asm.Annotations;
import com.codesmyth.droidcook.asm.Details;
import com.codesmyth.droidcook.asm.Maxs;
import com.codesmyth.droidcook.asm.Utils;

public final class OnCreateView implements Opcodes {

	private OnCreateView() { }

	public static void invoke(ClassVisitor cv, Details details, File file, boolean overridden) {
		Annotations ann = details.getAnnotations();

		Maxs maxs = new Maxs();
		maxs.setStack(1);
		maxs.setLocals(4);

		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "onCreateView", "(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;", null, null);
		mv.visitCode();

		// View for use in return, and possibly set below
		mv.visitInsn(ACONST_NULL);
		mv.visitVarInsn(ASTORE, 4);

		// determine where view is coming from. Order of precedence is
		// (1) original method return
		// (2) @ContentView annotation
		// (3) super.onCreateView
		if (overridden) {
			invokeOverridden(mv, maxs, details);
		} else if (ann.contains(details.getClassName(), Annotations.CONTENT_VIEW)) {
			Integer id = (Integer) ann.get(details.getClassName(), Annotations.CONTENT_VIEW, "value");
			invokeInflate(mv, maxs, details, id);
		} else {
			invokeSuper(mv, maxs, details);
		}

		for (String field : ann.namesFor(Annotations.VIEW_BY_ID)) {
			Integer id = (Integer) ann.get(field, Annotations.VIEW_BY_ID, "value");
			invokeFindViewById(mv, maxs, details, field, id);
		}

		for (String field : ann.namesFor(Annotations.EXTRA)) {
			String extraName = (String) ann.get(field, Annotations.EXTRA, "value");
			invokeGetArgument(mv, maxs, details, extraName, field);
		}

		for (String method : ann.namesFor(Annotations.ON_CREATE_VIEW)) {
			invokeVirtual(mv, maxs, details, method);
		}

		String anonName = Utils.nextInnerClassName(file, details.getClassName());
		for (String method : ann.namesFor(Annotations.ON_CLICK)) {

			int[] ids = (int[]) ann.get(method, Annotations.ON_CLICK, "value");
			if (ids == null) {
				ids = new int[]{details.getResourceId(Utils.normalizeName(method))};
			}

			for (int id : ids) {
				invokeClickListener(mv, maxs, details, anonName, id);
				Utils.newAnonymousInnerOnClick(details.getClassName(), anonName, method, file);
				anonName = Utils.nextInnerClassName(anonName);
			}
		}

		//
		invokeReturn(mv, maxs);
		mv.visitEnd();
	}

	/**
	 * Invoke super method and sets return to var for further use.
	 */
	private static void invokeSuper(MethodVisitor mv, Maxs maxs, Details details) {
		String superName = details.getSuperName();

		// TODO verify asm
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitMethodInsn(INVOKESPECIAL, superName, "onCreateView", "(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;");
		mv.visitVarInsn(ASTORE, 4);

		maxs.setStack(4);
		maxs.setLocals(5);
	}

	/**
	 * Invoke overridden method and sets return to var for further use.
	 */
	private static void invokeOverridden(MethodVisitor mv, Maxs maxs, Details details) {
		String className = details.getClassName();

		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "_onCreateView", "(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;");
		mv.visitVarInsn(ASTORE, 4);

		maxs.setStack(4);
		maxs.setLocals(5);
	}

	/**
	 * 
	 */
	private static void invokeVirtual(MethodVisitor mv, Maxs maxs, Details details, String methodName) {
		String className = details.getClassName();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitVarInsn(ALOAD, 3);
		// TODO allow signature to receive any set of arguments
		mv.visitMethodInsn(INVOKEVIRTUAL, className, methodName, "(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)V");
		maxs.setStack(4);
		maxs.setLocals(4);
	}

	/**
	 * Inflates view by given id and sets var for further use.
	 */
	private static void invokeInflate(MethodVisitor mv, Maxs maxs, Details details, Integer id) {
		String className = details.getClassName();

		if (id == null || id == 0) {
			int i = className.lastIndexOf("/") + 1;
			String idName = Utils.normalizeName(className.substring(i, className.length()));
			id = details.getResourceLayout(idName);
		}

		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(id);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitInsn(ICONST_0);
		mv.visitMethodInsn(INVOKEVIRTUAL, "android/view/LayoutInflater", "inflate", "(ILandroid/view/ViewGroup;Z)Landroid/view/View;");
		mv.visitVarInsn(ASTORE, 4);
		maxs.setStack(4);
		maxs.setLocals(5);
	}

	/**
	 * 
	 */
	private static void invokeGetArgument(MethodVisitor mv, Maxs maxs, Details details, String argName, String fieldName) {
		String className = details.getClassName();
		String fieldDesc = details.getFieldDesc(fieldName);
		String fieldType = details.getFieldType(fieldName);

		if (argName == null || "@null".equals(argName)) {
			argName = Utils.normalizeName(fieldName);
		}

		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "getArguments", "()Landroid/os/Bundle;");
		mv.visitLdcInsn(argName);
		mv.visitMethodInsn(INVOKEVIRTUAL, "android/os/Bundle", "get", "(Ljava/lang/String;)Ljava/lang/Object;");
		mv.visitTypeInsn(CHECKCAST, fieldType);
		mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldDesc);

		maxs.setStack(3);
		maxs.setLocals(1);
	}

	private static void invokeFindViewById(MethodVisitor mv, Maxs maxs, Details details, String fieldName, Integer id) {
		String className = details.getClassName();
		String fieldDesc = details.getFieldDesc(fieldName);
		String fieldType = details.getFieldType(fieldName);

		if (id == null || id == 0) {
			String idName = Utils.normalizeName(fieldName);
			id = details.getResourceId(idName);
		}

		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 4);
		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKEVIRTUAL, "android/view/View", "findViewById", "(I)Landroid/view/View;");
		mv.visitTypeInsn(CHECKCAST, fieldType);
		mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldDesc);
		maxs.setStack(4);
		maxs.setLocals(5);
	}

	private static void invokeClickListener(MethodVisitor mv, Maxs maxs, Details details, String anonName, Integer id) {
		String className = details.getClassName();
		mv.visitVarInsn(ALOAD, 4);
		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKEVIRTUAL, "android/view/View", "findViewById", "(I)Landroid/view/View;");
		mv.visitTypeInsn(NEW, anonName);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, anonName, "<init>", "(L" + className + ";)V");
		mv.visitMethodInsn(INVOKEVIRTUAL, "android/view/View", "setOnClickListener", "(Landroid/view/View$OnClickListener;)V");
		maxs.setStack(4);
	}

	/**
	 * Return inflated view.
	 */
	private static void invokeReturn(MethodVisitor mv, Maxs maxs) {
		mv.visitVarInsn(ALOAD, 4);
		mv.visitInsn(ARETURN);
		mv.visitMaxs(maxs.getStack(), maxs.getLocals());
	}
}
