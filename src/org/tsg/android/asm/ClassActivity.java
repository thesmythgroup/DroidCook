package org.tsg.android.asm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * TODO rework interface Handler to be a Visitor for chaining method bytecode generation.
 */
public class ClassActivity extends BaseClassVisitor implements Handler {

	private static final String CONTENT_VIEW = "Lorg/tsg/android/api/Annotations$ContentView;";
	private static final String VIEW_BY_ID = "Lorg/tsg/android/api/Annotations$ViewById;";
	private static final String ON_CREATE = "Lorg/tsg/android/api/Annotations$OnCreate;";

	private enum Override {
		ONCREATE
	}

	private List<Override> mUsed;

	public ClassActivity(ClassVisitor cv) {
		super(cv);
		setHandler(this);
		mUsed = new ArrayList<Override>();
	}

	public void visitFinalize() {
		if (!mUsed.contains(Override.ONCREATE)) {
			Map<String, BaseAnnotationVisitor> annotations = getAnnotationMap().get(getClassName());
			if (annotations != null && annotations.get(CONTENT_VIEW) != null) {
				BaseAnnotationVisitor bav = annotations.get(CONTENT_VIEW);
				onCreate(getVisitor(), getClassName(), getSuperName(), bav, false);
			}
		}
	}

	public boolean match(String name) {
		try {
			Override.valueOf(name.toUpperCase());
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public void handle(ClassVisitor cv, String className, String superName, String methodName, Map<String, BaseAnnotationVisitor> annotations) {
		Override override = Override.valueOf(methodName.toUpperCase());
		mUsed.add(override);

		switch (override) {
		case ONCREATE:
			BaseAnnotationVisitor as = null;
			if (annotations != null) {
				as = annotations.get(CONTENT_VIEW);
			}
			onCreate(cv, className, superName, as, true);
			break;
		default:
			System.out.println("TODO implement " + methodName);
			break;
		}
	}

	public void onCreate(ClassVisitor cv, String className, String superName, BaseAnnotationVisitor as, boolean createVirtual) {
		if (className == null) {
			throw new RuntimeException("Received null for className!");
		}

		Maxs maxs = new Maxs();
		maxs.incLocals(2); // this, savedState

		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "onCreate", "(Landroid/os/Bundle;)V", null, null);
		mv.visitCode();

		onCreateSuper(mv, maxs);

		// setContentView
		Map<String, BaseAnnotationVisitor> map = getAnnotationMap().get(getClassName());
		if (map != null && map.get(CONTENT_VIEW) != null) {
			setContentView(mv, maxs, (Integer) map.get(CONTENT_VIEW).get("value"));
		}

		// call original if exists
		if (createVirtual) {
			onCreateVirtual(mv, maxs, "_onCreate");
		}

	 	// TODO create container that facilitates the following with an iterator along with
		// other bits found in this package.

		// handle @ViewById
		for (Entry<String, Map<String, BaseAnnotationVisitor>> entry : getAnnotationMap().entrySet()) {
			String name = entry.getKey();

			for (Entry<String, BaseAnnotationVisitor> entry2 : entry.getValue().entrySet()) {
				String annotation = entry2.getKey();

				if (annotation.equals(VIEW_BY_ID)) {
					findViewById(mv, maxs, name, (Integer) entry2.getValue().get("value"));
				}
			}
		}

		// handle @OnCreate
		for (Entry<String, Map<String, BaseAnnotationVisitor>> entry : getAnnotationMap().entrySet()) {
			String name = entry.getKey();

			for (Entry<String, BaseAnnotationVisitor> entry2 : entry.getValue().entrySet()) {
				String annotation = entry2.getKey();

				if (annotation.equals(ON_CREATE)) {
					onCreateVirtual(mv, maxs, name);
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
		mv.visitMethodInsn(INVOKESPECIAL, getSuperName(), "onCreate", "(Landroid/os/Bundle;)V");
		maxs.incStack();
	}

	public void onCreateVirtual(MethodVisitor mv, Maxs maxs, String methodName) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, getClassName(), methodName, "(Landroid/os/Bundle;)V");
		maxs.incStack();
	}

	public void onCreateReturn(MethodVisitor mv, Maxs maxs) {
		mv.visitInsn(RETURN);
		mv.visitMaxs(maxs.getStack(), maxs.getLocals());
	}

	public void setContentView(MethodVisitor mv, Maxs maxs, Integer id) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKEVIRTUAL, getClassName(), "setContentView", "(I)V");
		maxs.incStack();
	}

	/**
	 * TODO get index of fieldNames and types for reference here to proper type
	 */
	public void findViewById(MethodVisitor mv, Maxs maxs, String fieldName, Integer id) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKEVIRTUAL, getClassName(), "findViewById", "(I)Landroid/view/View;");
		mv.visitTypeInsn(CHECKCAST, "android/widget/TextView");
		mv.visitFieldInsn(PUTFIELD, getClassName(), fieldName, "Landroid/widget/TextView;");
		maxs.incStack(3);
	}
}
