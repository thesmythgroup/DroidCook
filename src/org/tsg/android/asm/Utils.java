package org.tsg.android.asm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.*;

public final class Utils implements Opcodes {

	private Utils() { }

	/**
	 * Checks if androidClassName is assignable from className.
	 */
	public static boolean isAssignableFrom(String className, String androidClassName) {
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		try {
			Class a = loader.loadClass(className.replace("/", "."));
			Class b = loader.loadClass(androidClassName);
			if (b.isAssignableFrom(a)) {
				return true;
			}
		} catch (ClassNotFoundException e) {
			//
		}
		return false;
	}

	/**
	 * Normalizes a given CamelCase name to an android_identifier. Examples:
	 *
	 * mTextView -> text_view
	 * sDarkBlue -> dark_blue
	 * imageView -> image_view
	 */
	public static String normalizeName(String name) {
		// TODO handle sDarkBlue
		return name.replaceFirst("^m([A-Z][A-Za-z0-9]*$)", "$1").replaceAll("(.)([A-Z])", "$1_$2").toLowerCase();
	}

	public static String getFileName(String className) {
		int i = className.lastIndexOf("/");
		return className.substring(i+1, className.length()) + ".class";
	}

	public static String nextInnerClassName(File file, String className) {
		int i = 1;
		String fileName = file.getName().replace(".class", "");
		for (File f : file.getParentFile().listFiles()) {
			String name = f.getName().replace(".class", "");
			if (name.startsWith(fileName) && name.contains("$")) {
				try {
					int j = Integer.parseInt(name.split("\\$")[1]);
					if (j > i) {
						i = j;
					}
				} catch (Exception e) {
					continue;
				}
			}
		}
		return className + "$" + (i + 1);
	}

	public static String nextInnerClassName(String name) {
		if (!name.contains("$")) {
			throw new RuntimeException("Did not receive anonymous inner name: " + name);
		}
		String[] sp = name.split("\\$");
		int i = Integer.parseInt(sp[1]);
		return sp[0] + "$" + (i + 1);
	}

	public static void newAnonymousInnerOnClick(String className, String anonName, String methodName, File file) {
		File inner = new File(file.getParentFile(), getFileName(anonName));

		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;

		cw.visit(V1_6, ACC_SUPER, anonName, null, "java/lang/Object", new String[] {"android/view/View$OnClickListener"});
		// cw.visitAnnotation("Lorg/tsg/android/api/Annotations$NoTransform;", true);

		// cw.visitSource("MainActivity.java", null);

		cw.visitOuterClass(className, "onCreate", "(Landroid/os/Bundle;)V");
		cw.visitInnerClass(anonName, null, null, 0);
		cw.visitInnerClass("android/view/View$OnClickListener", "android/view/View", "OnClickListener", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE);

		cw.visitField(ACC_FINAL + ACC_SYNTHETIC, "this$0", "L" + className + ";", null, null).visitEnd();

		{
			mv = cw.visitMethod(0, "<init>", "(L" + className + ";)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitFieldInsn(PUTFIELD, anonName, "this$0", "L" + className + ";");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
			mv.visitInsn(RETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "L" + anonName + ";", null, l0, l1, 0);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}

		{
			mv = cw.visitMethod(ACC_PUBLIC, "onClick", "(Landroid/view/View;)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, anonName, "this$0", "L" + className + ";");
			mv.visitMethodInsn(INVOKEVIRTUAL, className, methodName, "()V");
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitInsn(RETURN);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLocalVariable("this", "L" + anonName + ";", null, l0, l2, 0);
			mv.visitLocalVariable("v", "Landroid/view/View;", null, l0, l2, 1);
			mv.visitMaxs(1, 2);
			mv.visitEnd();
		}

		cw.visitEnd();

		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(inner));
			bos.write(cw.toByteArray());
			bos.flush();
			bos.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
