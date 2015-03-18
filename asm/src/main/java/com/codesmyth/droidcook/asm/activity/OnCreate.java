package com.codesmyth.droidcook.asm.activity;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import com.codesmyth.droidcook.asm.Annotations;
import com.codesmyth.droidcook.asm.Details;
import com.codesmyth.droidcook.asm.Maxs;
import com.codesmyth.droidcook.asm.Utils;

public final class OnCreate implements Opcodes {

	private static final String ERROR = "@OnCreate - %s has desc %s. Must either be empty or (Landroid/os/Bundle;)V";

	private static List<String> sAccepts = (Arrays.asList(
		"()V",
		"(Landroid/os/Bundle;)V"
	));

	private OnCreate() { }

	public static void invoke(ClassVisitor cv, Details details, File file, boolean overridden) {

		Annotations ann = details.getAnnotations();

		Maxs maxs = new Maxs();
		maxs.setLocals(2);

		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "onCreate", "(Landroid/os/Bundle;)V", null, null);
		mv.visitCode();

		// immediately call overridden method. This has the affect of potentially nullifying work done
		// in the original onCreate but is the safest measure to take in case code, such as
		// requestWindowFeature, is still present which needs to be called before setContentView.
		if (overridden) {
			invokeOverridden(mv, maxs, details);
		} else {
			invokeSuper(mv, maxs, details);
		}

		if (ann.contains(details.getClassName(), Annotations.NO_TITLE)) {
			invokeNoTitle(mv, maxs, details);
		}

		if (ann.contains(details.getClassName(), Annotations.CONTENT_VIEW)) {
			Integer id = (Integer) ann.get(details.getClassName(), Annotations.CONTENT_VIEW, "value");
			invokeSetContentView(mv, maxs, details, id);
		}

		for (String field : ann.namesFor(Annotations.VIEW_BY_ID)) {
			Integer id = (Integer) ann.get(field, Annotations.VIEW_BY_ID, "value");
			invokeFindViewById(mv, maxs, details, field, id);
		}

		for (String field : ann.namesFor(Annotations.EXTRA)) {
			String extraName = (String) ann.get(field, Annotations.EXTRA, "value");
			invokeGetExtra(mv, maxs, details, extraName, field);
		}

		for (String method : ann.namesFor(Annotations.ON_CREATE)) {
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

	private static void invokeSuper(MethodVisitor mv, Maxs maxs, Details details) {
		String superName = details.getSuperName();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESPECIAL, superName, "onCreate", "(Landroid/os/Bundle;)V");
		maxs.setStack(2);
	}

	private static void invokeOverridden(MethodVisitor mv, Maxs maxs, Details details) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, details.getClassName(), "_onCreate", "(Landroid/os/Bundle;)V");
		maxs.setStack(2);
	}

	private static void invokeVirtual(MethodVisitor mv, Maxs maxs, Details details, String methodName) {
		String className = details.getClassName();
		String desc = details.getMethodDesc(methodName);

		if (!sAccepts.contains(desc)) {
			throw new RuntimeException(String.format(ERROR, methodName, desc));
		}

		mv.visitVarInsn(ALOAD, 0);

		if ("(Landroid/os/Bundle;)V".equals(desc)) {
			mv.visitVarInsn(ALOAD, 1);
		}

		mv.visitMethodInsn(INVOKEVIRTUAL, className, methodName, desc);
		maxs.setStack(1);
	}

	private static void invokeNoTitle(MethodVisitor mv, Maxs maxs, Details details) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(ICONST_1);
		mv.visitMethodInsn(INVOKEVIRTUAL, details.getClassName(), "requestWindowFeature", "(I)Z");
		mv.visitInsn(POP);
		maxs.setStack(2);
		maxs.setLocals(1);
	}

	private static void invokeSetContentView(MethodVisitor mv, Maxs maxs, Details details, Integer id) {
		String className = details.getClassName();
		mv.visitVarInsn(ALOAD, 0);

		if (id == null || id == 0) {
			int i = className.lastIndexOf("/") + 1;
			String idName = Utils.normalizeName(className.substring(i, className.length()));
			id = details.getResourceLayout(idName);
		}

		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "setContentView", "(I)V");
		maxs.setStack(1);
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
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "findViewById", "(I)Landroid/view/View;");
		mv.visitTypeInsn(CHECKCAST, fieldType);
		mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldDesc);
		maxs.setStack(3);
	}

	private static void invokeGetExtra(MethodVisitor mv, Maxs maxs, Details details, String extraName, String fieldName) {
		String className = details.getClassName();
		String fieldDesc = details.getFieldDesc(fieldName);
		String fieldType = details.getFieldType(fieldName);

		if (extraName == null || "@null".equals(extraName)) {
			extraName = Utils.normalizeName(fieldName);
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

	private static void invokeClickListener(MethodVisitor mv, Maxs maxs, Details details, String anonName, Integer id) {
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

	private static void invokeReturn(MethodVisitor mv, Maxs maxs) {
		mv.visitInsn(RETURN);
		mv.visitMaxs(maxs.getStack(), maxs.getLocals());
	}

}
