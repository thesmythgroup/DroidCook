package org.tsg.android.asm;

import org.objectweb.asm.Label;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassActivity extends ClassVisitor implements Opcodes {

	private static final String CONTENT_VIEW = "Lorg/tsg/android/api/Annotations$ContentView;";
	private static final String VIEW_BY_ID = "Lorg/tsg/android/api/Annotations$ViewById;";
	private static final String ON_CREATE = "Lorg/tsg/android/api/Annotations$OnCreate;";
	private static final String ON_CLICK = "Lorg/tsg/android/api/Annotations$OnClick;";

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

		
	}

	public void visitSource(String source, String debug) {
		mVisitor.visitSource(source, debug);

		if (mDetails.getAnnotations().exists(ON_CLICK)) {
			// TODO iterate over @OnClick's
			// TODO generate anon inner name
			String anonName = mDetails.getClassName() + "$1";

			mVisitor.visitInnerClass(anonName, null, null, 0);
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
		boolean overridden = mOverridden.contains(Override.ONCREATE);

		Maxs maxs = new Maxs();
		maxs.incLocals(2); // this, savedState

		MethodVisitor mv = mVisitor.visitMethod(ACC_PUBLIC, "onCreate", "(Landroid/os/Bundle;)V", null, null);
		mv.visitCode();

		onCreateSuper(mv, maxs);

		if (overridden) {
			onCreateVirtual(mv, maxs, "_onCreate");
		}

		if (ann.contains(className, CONTENT_VIEW)) {
			setContentView(mv, maxs, (Integer) ann.get(className, CONTENT_VIEW, "value"));
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

		if (ann.exists(ON_CLICK)) {
			for (String method : ann.namesFor(ON_CLICK)) {
				int[] ids = (int[]) ann.get(method, ON_CLICK, "value");
				for (int id : ids) {
					setOnClickListener(mv, maxs, id);
					Utils.newAnonymousInnerOnClick(mDetails.getClassName(), method, mFile);
				}
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
		maxs.incStack(2);
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
		String className = mDetails.getClassName();
		String fieldDesc = mDetails.getFieldDesc(fieldName);
		String fieldType = mDetails.getFieldType(fieldName);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "findViewById", "(I)Landroid/view/View;");
		mv.visitTypeInsn(CHECKCAST, fieldType);
		mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldDesc);
		maxs.incStack(3);
	}

	public void setOnClickListener(MethodVisitor mv, Maxs maxs, Integer id) {
		String className = mDetails.getClassName();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "findViewById", "(I)Landroid/view/View;");
		mv.visitTypeInsn(NEW, className + "$1");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className + "$1", "<init>", "(L" + className + ";)V");
		mv.visitMethodInsn(INVOKEVIRTUAL, "android/view/View", "setOnClickListener", "(Landroid/view/View$OnClickListener;)V");
		maxs.incStack(4);
	}

	private static void label(MethodVisitor mv, int i) {
		Label l = new Label();
		mv.visitLabel(l);
		mv.visitLineNumber(i, l);
	}
}
