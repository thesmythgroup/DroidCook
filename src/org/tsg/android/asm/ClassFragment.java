package org.tsg.android.asm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassFragment extends ClassVisitor implements Opcodes {

	private enum Override {
		ONCREATEVIEW,
		ONACTIVITYCREATED;

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

	public ClassFragment(ClassVisitor cv, Details details, File file) {
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
		onCreateView();
		onActivityCreated();
		mVisitor.visitEnd();
	}


	public void onCreateView() {

		Annotations ann = mDetails.getAnnotations();
		String className = mDetails.getClassName();
		String superName = mDetails.getSuperName();
		boolean overridden = mOverridden.contains(Override.ONCREATEVIEW);

		Maxs maxs = new Maxs();
		maxs.setStack(1);
		maxs.setLocals(4);

		MethodVisitor mv = mVisitor.visitMethod(ACC_PUBLIC, "onCreateView", "(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;", null, null);
		mv.visitCode();

		// View for use in return, and possibly set below
		mv.visitInsn(ACONST_NULL);
		mv.visitVarInsn(ASTORE, 4);

		if (overridden) {
			Methods.onCreateViewOriginal(mv, maxs, mDetails);
		} else if (ann.contains(className, Annotations.CONTENT_VIEW)) {
			Integer id = (Integer) ann.get(className, Annotations.CONTENT_VIEW, "value");
			Methods.onCreateViewInflate(mv, maxs, mDetails, id);
		}

		if (ann.exists(Annotations.VIEW_BY_ID)) {
			for (String field : ann.namesFor(Annotations.VIEW_BY_ID)) {
				Integer id = (Integer) ann.get(field, Annotations.VIEW_BY_ID, "value");
				Methods.onCreateViewInjectView(mv, maxs, mDetails, field, id);
			}
		}

		/* TODO getArguments()
		if (ann.exists(Annotations.EXTRA)) {
			for (String field : ann.namesFor(Annotations.EXTRA)) {
				String extraName = (String) ann.get(field, Annotations.EXTRA, "value");
				Methods.injectExtra(mv, maxs, mDetails, extraName, field);
			}
		}
		*/

		if (ann.exists(Annotations.ON_CREATE_VIEW)) {
			for (String method : ann.namesFor(Annotations.ON_CREATE_VIEW)) {
				Methods.onCreateViewVirtual(mv, maxs, mDetails, method);
			}
		}

		if (ann.exists(Annotations.ON_CLICK)) {
			String anonName = Utils.nextInnerClassName(mFile, mDetails.getClassName());
			for (String method : ann.namesFor(Annotations.ON_CLICK)) {
				int[] ids = (int[]) ann.get(method, Annotations.ON_CLICK, "value");
				for (int id : ids) {
					setOnClickListener(mv, maxs, mDetails, anonName, id);
					Utils.newAnonymousInnerOnClick(mDetails.getClassName(), anonName, method, mFile);
					anonName = Utils.nextInnerClassName(anonName);
				}
			}
		}

		//
		Methods.onCreateViewReturn(mv, maxs, mDetails);
		mv.visitEnd();
	}

	public void onActivityCreated() {
		Annotations ann = mDetails.getAnnotations();
		String className = mDetails.getClassName();
		String superName = mDetails.getSuperName();
		boolean overridden = mOverridden.contains(Override.ONACTIVITYCREATED);

		Maxs maxs = new Maxs();
		maxs.setLocals(2);

		MethodVisitor mv = mVisitor.visitMethod(ACC_PUBLIC, "onActivityCreated", "(Landroid/os/Bundle;)V", null, null);
		mv.visitCode();

		Methods.onActivityCreatedSuper(mv, maxs, mDetails);

		if (overridden) {
			Methods.onCreateVirtual(mv, maxs, mDetails, "_onActivityCreated");
		}

		if (ann.exists(Annotations.ON_CREATE)) {
			for (String method : ann.namesFor(Annotations.ON_CREATE)) {
				Methods.onCreateVirtual(mv, maxs, mDetails, method);
			}
		}

		//
		mv.visitInsn(RETURN);
		mv.visitMaxs(maxs.getStack(), maxs.getLocals());
		mv.visitEnd();
	}

	public static void setOnClickListener(MethodVisitor mv, Maxs maxs, Details details, String anonName, Integer id) {
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
}
