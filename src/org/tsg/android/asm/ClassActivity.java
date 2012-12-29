package org.tsg.android.asm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassActivity extends ClassVisitor implements Opcodes {

	private static final String CONTENT_VIEW = "Lorg/tsg/android/api/Annotations$ContentView;";
	private static final String VIEW_BY_ID = "Lorg/tsg/android/api/Annotations$ViewById;";
	private static final String ON_CREATE = "Lorg/tsg/android/api/Annotations$OnCreate;";

	private enum Override {
		ONCREATE;

		public static boolean match(String name) {
			if (from(name) != null) {
				return true;
			}
			return false;
		}

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

	public ClassActivity(ClassVisitor cv, Details details) {
		super(ASM4, cv);
		mVisitor = cv;
		mDetails = details;
		mOverridden = new ArrayList<Override>();
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
		boolean overridden = mOverridden.contains(Override.ONCREATE);

		Maxs maxs = new Maxs();
		maxs.incLocals(2); // this, savedState

		MethodVisitor mv = mVisitor.visitMethod(ACC_PUBLIC, "onCreate", "(Landroid/os/Bundle;)V", null, null);
		mv.visitCode();

		onCreateSuper(mv, maxs);

		if (ann.contains(className, CONTENT_VIEW)) {
			setContentView(mv, maxs, (Integer) ann.get(className, CONTENT_VIEW, "value"));
		}

		// call original if exists
		if (overridden) {
			onCreateVirtual(mv, maxs, "_onCreate");
		}
		
		if (ann.exists(VIEW_BY_ID)) {
			for (String field : ann.namesFor(VIEW_BY_ID)) {
				Integer id = (Integer) ann.get(field, VIEW_BY_ID, "value");
				findViewById(mv, maxs, field, id);
			}
		}

		if (ann.exists(ON_CREATE)) {
			for (String method : ann.namesFor(ON_CREATE)) {
				onCreateVirtual(mv, maxs, method);
			}
		}

		//
		onCreateReturn(mv, maxs);
		mv.visitEnd();
	}

	public void onCreateSuper(MethodVisitor mv, Maxs maxs) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESPECIAL, mDetails.getSuperName(), "onCreate", "(Landroid/os/Bundle;)V");
		maxs.incStack();
	}

	public void onCreateVirtual(MethodVisitor mv, Maxs maxs, String methodName) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, mDetails.getClassName(), methodName, "(Landroid/os/Bundle;)V");
		maxs.incStack();
	}

	public void onCreateReturn(MethodVisitor mv, Maxs maxs) {
		mv.visitInsn(RETURN);
		mv.visitMaxs(maxs.getStack(), maxs.getLocals());
	}

	public void setContentView(MethodVisitor mv, Maxs maxs, Integer id) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKEVIRTUAL, mDetails.getClassName(), "setContentView", "(I)V");
		maxs.incStack();
	}

	public void findViewById(MethodVisitor mv, Maxs maxs, String fieldName, Integer id) {
		String fieldDesc = mDetails.getFieldDesc(fieldName);
		String fieldType = mDetails.getFieldType(fieldName);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKEVIRTUAL, mDetails.getClassName(), "findViewById", "(I)Landroid/view/View;");
		mv.visitTypeInsn(CHECKCAST, fieldType);
		mv.visitFieldInsn(PUTFIELD, mDetails.getClassName(), fieldName, fieldDesc);
		maxs.incStack(3);
	}
}
