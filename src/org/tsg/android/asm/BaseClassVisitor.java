package org.tsg.android.asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public abstract class BaseClassVisitor extends ClassVisitor implements Opcodes {

	private Handler mHandler;

	private String mClassName;
	private String mSuperName;
	private ClassVisitor mVisitor;
	private List<String> mQueue;

	// reference to class/field/method to watch for annotations
	private String mWatching;

	private Map<String, Map<String, BaseAnnotationVisitor>> mAnnotationMap;
	private Map<String, BaseAnnotationVisitor> mAnnotations;

	// transforms the given class when true, set in source with @Transform(boolean)
	private boolean mTransform = true;

	// called right before visitEnd() to finish any pending work if mTransform == true
	public abstract void visitFinalize();

	public BaseClassVisitor(ClassVisitor cv) {
		super(ASM4, cv);
		mVisitor = cv;
		mQueue = new ArrayList<String>();
		mAnnotationMap = new HashMap<String, Map<String, BaseAnnotationVisitor>>();
		mAnnotations = new HashMap<String, BaseAnnotationVisitor>();
	}

	public void setHandler(Handler handler) {
		mHandler = handler;
	}

	public String getClassName() {
		return mClassName;
	}

	public String getSuperName() {
		return mSuperName;
	}

	public ClassVisitor getVisitor() {
		return mVisitor;
	}

	public Map<String, Map<String, BaseAnnotationVisitor>> getAnnotationMap() {
		return mAnnotationMap;
	}

	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		if (mHandler == null) {
			throw new RuntimeException("Failed to call setHandler!");
		}
		mClassName = name;
		mSuperName = superName;
		checkAnnotation(name);
		mVisitor.visit(version, access, name, signature, superName, interfaces);
	}

	public void visitSource(String source, String debug) {
		mVisitor.visitSource(source, debug);
	}

	public void visitOuterClass(String owner, String name, String desc) {
		checkAnnotation(name);
		mVisitor.visitOuterClass(owner, name, desc);
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		System.out.println("### visitAnnotation Class " + desc);
		AnnotationVisitor av = mVisitor.visitAnnotation(desc, visible);
		BaseAnnotationVisitor as = new BaseAnnotationVisitor(av);
		mAnnotations.put(desc, as);
		return as;
	}

	public void visitAttribute(Attribute attr) {
		mVisitor.visitAttribute(attr);
	}

	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		// checkAnnotation(name);
		mVisitor.visitInnerClass(name, outerName, innerName, access);
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		checkAnnotation(name);
		FieldVisitor fv = mVisitor.visitField(access, name, desc, signature, value);
		BaseFieldVisitor bfv = new BaseFieldVisitor(fv, mAnnotations);
		return bfv;
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		checkAnnotation(name);
		MethodVisitor mv = null;
		if (mHandler.match(name) && mTransform) {
			mQueue.add(name);
			// TODO strip calls to super
			mv = mVisitor.visitMethod(access, "_" + name, desc, signature, exceptions);
		} else {
			mv = mVisitor.visitMethod(access, name, desc, signature, exceptions);
		}

		BaseMethodVisitor bmv = new BaseMethodVisitor(mv, mAnnotations);
		return bmv;
	}

	public void visitEnd() {
		checkAnnotation(null);

		if (mTransform) {
			for (String methodName : mQueue) {
				mHandler.handle(mVisitor, mClassName, mSuperName, methodName, mAnnotationMap.get(methodName));
			}
			visitFinalize();
		}
		mVisitor.visitEnd();
	}

	public void checkAnnotation(String name) {
		if (mWatching == null) {
			mWatching = name;
		}

		System.out.println("$$$ checkAnnotation watching " + mWatching);

		if (!mAnnotations.isEmpty()) {
			HashMap<String, BaseAnnotationVisitor> map = new HashMap<String, BaseAnnotationVisitor>();
			map.putAll(mAnnotations);

			mAnnotationMap.put(mWatching, map);
			mAnnotations.clear();

			System.out.println("$$$ checkAnnotation found " + mWatching);
			for (String key : map.keySet()) {
				System.out.println("$$$ checkAnnotation key " + key);
			}

			// check if @NoTransform is set on class
			if (mWatching.equals(mClassName)) {
				BaseAnnotationVisitor bav = map.get("Lorg/tsg/android/api/Annotations$NoTransform;");
				if (bav != null) {
					mTransform = false;
					System.out.println("Disabling transform on " + mClassName);
				}
				// prevent future transforms on this class file
				if (mTransform) {
					mVisitor.visitAnnotation("Lorg/tsg/android/api/Annotations$NoTransform;", true);
				}
			}
		}

		mWatching = name;
	}
}
