package org.tsg.android.asm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public abstract class BaseClassVisitor extends ClassVisitor implements Opcodes {

	public abstract void onVisitEnd();

	private List<String> mOverridden = new ArrayList<String>();

	private List<String> mOverrides;
	private ClassVisitor mVisitor;
	private Details mDetails;
	private File mFile;

	private boolean mHandledOnClick;

	public BaseClassVisitor(ClassVisitor cv, Details details, File file, List<String> overrides) {
		super(ASM4, cv);
		mVisitor = cv;
		mDetails = details;
		mFile = file;
		mOverrides = overrides;
	}

	protected boolean overridden(String name) {
		return mOverridden.contains(name);
	}

	protected Details getDetails() {
		return mDetails;
	}

	protected ClassVisitor getVisitor() {
		return mVisitor;
	}

	protected File getFile() {
		return mFile;
	}

	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		mVisitor.visit(version, access, name, signature, superName, interfaces);
	}

	public void visitSource(String source, String debug) {
		mVisitor.visitSource(source, debug);
	}

	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		if (!mHandledOnClick) {
			mHandledOnClick = true;
			Annotations ann = mDetails.getAnnotations();
			if (ann.exists(Annotations.ON_CLICK)) {
				String anonName = Utils.nextInnerClassName(mFile, mDetails.getClassName());
				for (String method : ann.namesFor(Annotations.ON_CLICK)) {

					int[] ids = (int[]) ann.get(method, Annotations.ON_CLICK, "value");
					int len = (ids == null) ? 0 : ids.length;

					for (int i = 0; i < len; i++) {
						mVisitor.visitInnerClass(anonName, null, null, 0);
						anonName = Utils.nextInnerClassName(anonName);
					}
				}
				mVisitor.visitInnerClass("android/view/View$OnClickListener", "android/view/View", "OnClickListener", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE);
			}
		}

		mVisitor.visitInnerClass(name, outerName, innerName, access);
	}

	/**
	 *
	 */
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (mOverrides == null || !mOverrides.contains(name)) {
			return mVisitor.visitMethod(access, name, desc, signature, exceptions);
		}

		mOverridden.add(name);
		// TODO strip calls to super
		MethodVisitor mv = mVisitor.visitMethod(access, "_" + name, desc, signature, exceptions);
		return mv;
	}

	public void visitEnd() {
		onVisitEnd();
		mVisitor.visitEnd();
	}

}
