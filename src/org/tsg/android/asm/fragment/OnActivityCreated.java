package org.tsg.android.asm.fragment;

import java.util.Arrays;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.tsg.android.asm.Annotations;
import org.tsg.android.asm.Details;
import org.tsg.android.asm.Maxs;

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

}
