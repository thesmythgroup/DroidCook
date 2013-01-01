package org.tsg.android.asm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

public class Main extends ClassVisitor implements Opcodes {

	private enum AndroidClass {
		ACTIVITY("android.app.Activity"),
		FRAGMENT_ACTIVITY("android.support.v4.app.FragmentActivity"),
		FRAGMENT("android.support.v4.app.Fragment");

		private String mName;

		private AndroidClass(String name) {
			mName = name;
		}

		@SuppressWarnings("unchecked")
		public static ClassVisitor getVisitorFor(String name, ClassVisitor visitor, Details details, File file) {

			AndroidClass selected = null;

			for (AndroidClass androidClass : AndroidClass.values()) {
				try {
					ClassLoader loader = ClassLoader.getSystemClassLoader();
					Class a = loader.loadClass(name.replace("/", "."));
					Class b = loader.loadClass(androidClass.mName);
					if (b.isAssignableFrom(a)) {
						selected = androidClass;
						break;
					}
				} catch (IllegalArgumentException e) {
					System.out.println(e.getMessage());
					continue;
				} catch (ClassNotFoundException e) {
					System.out.println(e.getMessage());
					continue;
				}
			}

			if (selected == null) {
				return null;
			}

			switch (selected) {
			case ACTIVITY:
			case FRAGMENT_ACTIVITY:
				return new ClassActivity(visitor, details, file);
			default:
				System.out.println("TODO Implement ClassVisitor for " + name);
				return null;
			}
		}
	}

	private File mFile;
	private ClassVisitor mVisitor;
	private Details mDetails;

	public Main(ClassVisitor cv, Details details, File file) {
		super(ASM4, cv);
		mVisitor = cv;
		mDetails = details;
		mFile = file;
	}

	/**
	 * Match superName against known class handlers
	 */
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		ClassVisitor visitor = AndroidClass.getVisitorFor(superName, mVisitor, mDetails, mFile);
		if (visitor != null) {
			System.out.println(name);
			mVisitor = visitor;
		}
		mVisitor.visit(version, access, name, signature, superName, interfaces);
		// prevent future transforms of this class file
		mVisitor.visitAnnotation("Lorg/tsg/android/api/Annotations$NoTransform;", true);
	}

	public void visitSource(String source, String debug) {
		mVisitor.visitSource(source, debug);
	}

	public void visitOuterClass(String owner, String name, String desc) {
		mVisitor.visitOuterClass(owner, name, desc);
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return mVisitor.visitAnnotation(desc, visible);
	}

	public void visitAttribute(Attribute attr) {
		mVisitor.visitAttribute(attr);
	}

	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		mVisitor.visitInnerClass(name, outerName, innerName, access);
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		return mVisitor.visitField(access, name, desc, signature, value);
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		return mVisitor.visitMethod(access, name, desc, signature, exceptions);
	}

	public void visitEnd() {
		mVisitor.visitEnd();
	}

	public abstract static class FileVisitor {
		public abstract void visitFile(File f);
	}

	public static void listFiles(File f, FileVisitor v) {
		if (v == null) {
			throw new RuntimeException("Received null FileVisitor!");
		}
		File[] files = f.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				listFiles(file, v);
			} else if (file.isFile() && file.getPath().endsWith(".class")) {
				String p = file.getPath();
				if (p.contains("org/tsg/android") || p.contains("R$") || p.contains("R.class") || p.contains("BuildConfig.class")) {
					continue;
				}
				v.visitFile(file);
			}
		}
	}

	public static void main(String[] args) throws IOException {

		boolean debug = false;
		Printer printer = null;

		if (args.length == 0) {
			throw new RuntimeException("usage: Main [path]");
		}

		if (args.length == 2) {
			if ("-debug".equals(args[1])) {
				debug = true;
				printer = new Textifier();
			} else if ("-asmifier".equals(args[1])) {
				debug = true;
				printer = new ASMifier();
			}
		}

		final List<File> clsFiles = new ArrayList<File>();
		listFiles(new File(args[0]), new FileVisitor() {
			public void visitFile(File f) {
				clsFiles.add(f);
			}
		});

		for (File file : clsFiles) {
			RandomAccessFile f = new RandomAccessFile(file, "r");
			byte[] cls = new byte[(int) f.length()];
			f.read(cls);

			Details details = DetailsVisitor.getDetails(cls);

			// System.out.println(details);

			if (details.noTransform()) {
				System.out.println("skipping " + details.getClassName());
				continue;
			}

			ClassReader cr = new ClassReader(cls);
			ClassWriter cw = new ClassWriter(cr, 0);

			ClassVisitor cv = cw;

			if (debug) {
				cv = new TraceClassVisitor(cv, printer, new PrintWriter(System.out, true));
			}

			cv = new Main(cv, details, file);
			cr.accept(cv, 0);

			// write transformed class out
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			bos.write(cw.toByteArray());
			bos.flush();
			bos.close();
		}
	}
}
