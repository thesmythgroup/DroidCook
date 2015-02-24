package com.codesmyth.android.asm.fragment;

import java.util.Arrays;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import com.codesmyth.android.asm.Annotations;
import com.codesmyth.android.asm.Details;
import com.codesmyth.android.asm.Maxs;
import com.codesmyth.android.asm.Utils;

public final class OnActivityCreated implements Opcodes {

	private static final String ERROR = "@OnActivityCreated - %s has desc %s. Must either be empty or (Landroid/os/Bundle;)V";

	private static List<String> sAccepts = (Arrays.asList(
		"()V",
		"(Landroid/os/Bundle;)V"
	));

	private OnActivityCreated() { }

	public static void invoke(ClassVisitor cv, Details details, boolean overridden) {
		Annotations ann = details.getAnnotations();

		Maxs maxs = new Maxs();
		maxs.setLocals(2);

		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "onActivityCreated", "(Landroid/os/Bundle;)V", null, null);
		mv.visitCode();

		invokeSuper(mv, maxs, details);

		if (overridden) {
			invokeOverridden(mv, maxs, details);
		}

		for (String field : ann.namesFor(Annotations.RESOURCE)) {
			Integer id = (Integer) ann.get(field, Annotations.RESOURCE, "value");
			invokeResources(mv, maxs, details, field, id);
		}

		for (String field : ann.namesFor(Annotations.PREFS)) {
			String prefsName = (String) ann.get(field, Annotations.PREFS, "value");
			invokePrefs(mv, maxs, details, field, prefsName);
		}

		for (String method : ann.namesFor(Annotations.ON_ACTIVITY_CREATED)) {
			invokeVirtual(mv, maxs, details, method);
		}

		//
		invokeReturn(mv, maxs);
		mv.visitEnd();
	}

	private static void invokeSuper(MethodVisitor mv, Maxs maxs, Details details) {
		String superName = details.getSuperName();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESPECIAL, superName, "onActivityCreated", "(Landroid/os/Bundle;)V");
		maxs.setStack(2);
		maxs.setLocals(2);
	}

	private static void invokeOverridden(MethodVisitor mv, Maxs maxs, Details details) {
		String className = details.getClassName();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "_onActivityCreated", "(Landroid/os/Bundle;)V");
		maxs.setStack(2);
		maxs.setLocals(2);
	}

	/**
	 * Invoke method, passing in Bundle if in descriptor.
	 *
	 * @throws RuntimeException if method descriptor doesn't match accepted values.
	 */
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

	/**
	 * Return null.
	 */
	private static void invokeReturn(MethodVisitor mv, Maxs maxs) {
		mv.visitInsn(RETURN);
		mv.visitMaxs(maxs.getStack(), maxs.getLocals());
	}

	private static void invokeResources(MethodVisitor mv, Maxs maxs, Details details, String fieldName, Integer id) {
		String fieldDesc = details.getFieldDesc(fieldName);

		System.out.println("fieldDesc is " + fieldDesc);

		if ("I".equals(fieldDesc)) {
			invokeResourceInteger(mv, maxs, details, fieldName, id);
		} else if ("Z".equals(fieldDesc)) {
			invokeResourceBoolean(mv, maxs, details, fieldName, id);
		} else if ("Ljava/lang/String;".equals(fieldDesc)) {
			invokeResourceString(mv, maxs, details, fieldName, id);
		} else if ("Landroid/view/animation/Animation;".equals(fieldDesc)) {
			invokeResourceAnimation(mv, maxs, details, fieldName, id);
		} else if ("[I".equals(fieldDesc)) {
			invokeResourceIntArray(mv, maxs, details, fieldName, id);
		} else if ("[Ljava/lang/String;".equals(fieldDesc)) {
			invokeResourceStringArray(mv, maxs, details, fieldName, id);
		}
	}

	private static void invokeResourceAnimation(MethodVisitor mv, Maxs maxs, Details details, String fieldName, Integer id) {
		String className = details.getClassName();

		if (id == null || id == 0) {
			String idName = Utils.normalizeName(fieldName);
			id = details.getResourceAnimation(idName);
		}

		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "getActivity", "()Landroid/support/v4/app/FragmentActivity;");
		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKESTATIC, "android/view/animation/AnimationUtils", "loadAnimation", "(Landroid/content/Context;I)Landroid/view/animation/Animation;");
		mv.visitFieldInsn(PUTFIELD, className, fieldName, "Landroid/view/animation/Animation;");
		maxs.setStack(3);
		maxs.setLocals(1);
	}

	private static void invokeResourceInteger(MethodVisitor mv, Maxs maxs, Details details, String fieldName, Integer id) {
		if (id == null || id == 0) {
			String idName = Utils.normalizeName(fieldName);
			id = details.getResourceInteger(idName);
		}

		invokeResource(mv, maxs, details, fieldName, id, "getInteger");
	}

	private static void invokeResourceBoolean(MethodVisitor mv, Maxs maxs, Details details, String fieldName, Integer id) {
		if (id == null || id == 0) {
			String idName = Utils.normalizeName(fieldName);
			id = details.getResourceBoolean(idName);
		}

		invokeResource(mv, maxs, details, fieldName, id, "getBoolean");
	}

	private static void invokeResourceString(MethodVisitor mv, Maxs maxs, Details details, String fieldName, Integer id) {
		if (id == null || id == 0) {
			String idName = Utils.normalizeName(fieldName);
			id = details.getResourceString(idName);
		}

		invokeResource(mv, maxs, details, fieldName, id, "getString");
	}

	private static void invokeResourceIntArray(MethodVisitor mv, Maxs maxs, Details details, String fieldName, Integer id) {
		if (id == null || id == 0) {
			String idName = Utils.normalizeName(fieldName);
			id = details.getResourceArray(idName);
		}

		invokeResource(mv, maxs, details, fieldName, id, "getIntArray");
	}

	private static void invokeResourceStringArray(MethodVisitor mv, Maxs maxs, Details details, String fieldName, Integer id) {
		if (id == null || id == 0) {
			String idName = Utils.normalizeName(fieldName);
			id = details.getResourceArray(idName);
		}

		invokeResource(mv, maxs, details, fieldName, id, "getStringArray");
	}

	private static void invokeResource(MethodVisitor mv, Maxs maxs, Details details, String fieldName, Integer id, String methodName) {
		String className = details.getClassName();
		String fieldDesc = details.getFieldDesc(fieldName);

		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "getActivity", "()Landroid/support/v4/app/FragmentActivity;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "android/support/v4/app/FragmentActivity", "getResources", "()Landroid/content/res/Resources;");
		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKEVIRTUAL, "android/content/res/Resources", methodName, "(I)" + fieldDesc);
		mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldDesc);
		maxs.setStack(3);
		maxs.setLocals(1);
	}

	private static void invokePrefs(MethodVisitor mv, Maxs maxs, Details details, String fieldName, String prefsName) {
		String className = details.getClassName();

		if (prefsName == null || "@null".equals(prefsName)) {
			prefsName = "preferences";
		}

		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "getActivity", "()Landroid/support/v4/app/FragmentActivity;");
		mv.visitLdcInsn(prefsName);
		mv.visitInsn(ICONST_0);
		mv.visitMethodInsn(INVOKEVIRTUAL, "android/support/v4/app/FragmentActivity", "getSharedPreferences", "(Ljava/lang/String;I)Landroid/content/SharedPreferences;");
		mv.visitFieldInsn(PUTFIELD, className, fieldName, "Landroid/content/SharedPreferences;");
		maxs.setStack(4);
		maxs.setLocals(1);
	}

}
