package org.tsg.android.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class Methods implements Opcodes {

	private static final String EMPTY_DESC = "()V";

	private Methods() { }

	/* Utils */

	public static String normalizeName(String name) {
		return name.replaceFirst("^m([A-Z][A-Za-z0-9]*$)", "$1").replaceAll("(.)([A-Z])", "$1_$2").toLowerCase();
	}

	/* Activities */

	public static void onCreateSuper(MethodVisitor mv, Maxs maxs, Details details) {
		String superName = details.getSuperName();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESPECIAL, superName, "onCreate", "(Landroid/os/Bundle;)V");
		maxs.setStack(2);
	}

	public static void onCreateVirtual(MethodVisitor mv, Maxs maxs, Details details, String methodName) {
		String className = details.getClassName();
		String desc = details.getMethodDesc(methodName);
		if (desc == null) {
			desc = "(Landroid/os/Bundle;)V";
		}

		mv.visitVarInsn(ALOAD, 0);

		if ("(Landroid/os/Bundle;)V".equals(desc)) {
			mv.visitVarInsn(ALOAD, 1);
		} else if (!EMPTY_DESC.equals(desc)) {
			throw new RuntimeException("@OnCreate - Unrecognized desc for " + methodName + ": " + desc + ". Must either be empty or (Landroid/os/Bundle;)V");
		}

		mv.visitMethodInsn(INVOKEVIRTUAL, className, methodName, desc);
		maxs.setStack(1);
	}

	public static void onCreateReturn(MethodVisitor mv, Maxs maxs) {
		mv.visitInsn(RETURN);
		mv.visitMaxs(maxs.getStack(), maxs.getLocals());
	}

	public static void setContentView(MethodVisitor mv, Maxs maxs, Details details, Integer id) {
		String className = details.getClassName();
		mv.visitVarInsn(ALOAD, 0);

		if (id == null || id == 0) {
			int i = className.lastIndexOf("/") + 1;
			String idName = normalizeName(className.substring(i, className.length()));
			id = details.getResourceLayout(idName);
		}

		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "setContentView", "(I)V");
		maxs.setStack(1);
	}

	public static void findViewById(MethodVisitor mv, Maxs maxs, Details details, String fieldName, Integer id) {
		String className = details.getClassName();
		String fieldDesc = details.getFieldDesc(fieldName);
		String fieldType = details.getFieldType(fieldName);

		if (id == null || id == 0) {
			String idName = normalizeName(fieldName);
			id = details.getResourceId(idName);
		}

		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "findViewById", "(I)Landroid/view/View;");
		mv.visitTypeInsn(CHECKCAST, fieldType);
		mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldDesc);
		maxs.setStack(3);
	}

	public static void setOnClickListener(MethodVisitor mv, Maxs maxs, Details details, String anonName, Integer id) {
		String className = details.getClassName();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "findViewById", "(I)Landroid/view/View;");
		mv.visitTypeInsn(NEW, anonName);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, anonName, "<init>", "(L" + className + ";)V");
		mv.visitMethodInsn(INVOKEVIRTUAL, "android/view/View", "setOnClickListener", "(Landroid/view/View$OnClickListener;)V");
		maxs.setStack(4);
	}

	public static void injectExtra(MethodVisitor mv, Maxs maxs, Details details, String extraName, String fieldName) {
		String className = details.getClassName();
		String fieldDesc = details.getFieldDesc(fieldName);
		String fieldType = details.getFieldType(fieldName);

		if (extraName == null || "@null".equals(extraName)) {
			extraName = normalizeName(fieldName);
		}

		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "getIntent", "()Landroid/content/Intent;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "android/content/Intent", "getExtras", "()Landroid/os/Bundle;");
		mv.visitLdcInsn(extraName);
		mv.visitMethodInsn(INVOKEVIRTUAL, "android/os/Bundle", "get", "(Ljava/lang/String;)Ljava/lang/Object;");
		mv.visitTypeInsn(CHECKCAST, fieldType);
		mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldDesc);

		maxs.setStack(3);
		maxs.setLocals(1);

	}

	/* Fragments */

	public static void onCreateViewInflate(MethodVisitor mv, Maxs maxs, Details details, Integer id) {
		String className = details.getClassName();

		if (id == null || id == 0) {
			int i = className.lastIndexOf("/") + 1;
			String idName = normalizeName(className.substring(i, className.length()));
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

	public static void onCreateViewInjectView(MethodVisitor mv, Maxs maxs, Details details, String fieldName, Integer id) {
		String className = details.getClassName();
		String fieldDesc = details.getFieldDesc(fieldName);
		String fieldType = details.getFieldType(fieldName);

		if (id == null || id == 0) {
			String idName = normalizeName(fieldName);
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

	public static void onCreateViewVirtual(MethodVisitor mv, Maxs maxs, Details details, String methodName) {
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

	public static void onCreateViewOriginal(MethodVisitor mv, Maxs maxs, Details details) {
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

	public static void onCreateViewReturn(MethodVisitor mv, Maxs maxs, Details details) {
		String className = details.getClassName();

		mv.visitVarInsn(ALOAD, 4);
		mv.visitInsn(ARETURN);

		/*
		mv.visitLocalVariable("this", "L" + className + ";", null, null, null, 0);
		mv.visitLocalVariable("inflater", "Landroid/view/LayoutInflater;", null, null, null, 1);
		mv.visitLocalVariable("container", "Landroid/view/ViewGroup;", null, null, null, 2);
		mv.visitLocalVariable("savedState", "Landroid/os/Bundle;", null, null, null, 3);
		mv.visitLocalVariable("v", "Landroid/view/View;", null, null, null, 4);
		*/

		mv.visitMaxs(maxs.getStack(), maxs.getLocals());
	}

	public static void onActivityCreatedSuper(MethodVisitor mv, Maxs maxs, Details details) {
		String superName = details.getSuperName();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESPECIAL, superName, "onActivityCreated", "(Landroid/os/Bundle;)V");
		maxs.setStack(2);
		maxs.setLocals(2);
	}

	public static void onActivityCreatedVirtual(MethodVisitor mv, Maxs maxs, Details details, String methodName) {
		String className = details.getClassName();
		String desc = details.getMethodDesc(methodName);
		if (desc == null) {
			desc = "(Landroid/os/Bundle;)V";
		}

		mv.visitVarInsn(ALOAD, 0);

		if ("(Landroid/os/Bundle;)V".equals(desc)) {
			mv.visitVarInsn(ALOAD, 1);
		} else if (!EMPTY_DESC.equals(desc)) {
			throw new RuntimeException(
				"@OnActivityCreated - Unrecognized desc for " + methodName + ": " + desc
				+ ". Must either be empty or (Landroid/os/Bundle;)V"
			);
		}

		mv.visitMethodInsn(INVOKEVIRTUAL, className, methodName, desc);
		maxs.setStack(1);
	}
}
