package org.tsg.android.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class Methods implements Opcodes {

	private Methods() { }

	public static void onCreateSuper(MethodVisitor mv, Maxs maxs, Details details) {
		String superName = details.getSuperName();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESPECIAL, superName, "onCreate", "(Landroid/os/Bundle;)V");
		maxs.setStack(2);
	}

	public static void onCreateVirtual(MethodVisitor mv, Maxs maxs, Details details, String methodName) {
		String className = details.getClassName();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, methodName, "(Landroid/os/Bundle;)V");
		maxs.setStack(1);
	}

	public static void onCreateReturn(MethodVisitor mv, Maxs maxs) {
		mv.visitInsn(RETURN);
		mv.visitMaxs(maxs.getStack(), maxs.getLocals());
	}

	public static void setContentView(MethodVisitor mv, Maxs maxs, Details details, Integer id) {
		String className = details.getClassName();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "setContentView", "(I)V");
		maxs.setStack(1);
	}

	public static void findViewById(MethodVisitor mv, Maxs maxs, Details details, String fieldName, Integer id) {
		String className = details.getClassName();
		String fieldDesc = details.getFieldDesc(fieldName);
		String fieldType = details.getFieldType(fieldName);
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
}
