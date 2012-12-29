package org.tsg.android.asm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

public class Main extends ClassVisitor implements Opcodes {

	private enum ClsVisitor {
		ANDROID_APP_ACTIVITY;

		public static ClassVisitor getVisitorFor(String name, ClassVisitor visitor, Details details) {
			try {
				ClsVisitor cls = ClsVisitor.valueOf(name.replace("/", "_").toUpperCase());
				switch (cls) {
				case ANDROID_APP_ACTIVITY:
					return new ClassActivity(visitor, details);
				default:
					System.out.println("TODO Implement ClassVisitor for " + name);
					return null;
				}
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}

	private ClassVisitor mVisitor;
	private Details mDetails;

	public Main(ClassVisitor cv, Details details) {
		super(ASM4, cv);
		mVisitor = cv;
		mDetails = details;
	}

	/**
	 * Match superName against known class handlers
	 */
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		ClassVisitor visitor = ClsVisitor.getVisitorFor(superName, mVisitor, mDetails);
		if (visitor != null) {
			System.out.println(name);
			mVisitor = visitor;
		}
		mVisitor.visit(version, access, name, signature, superName, interfaces);
		// prevent future transforms of this class file
		mVisitor.visitAnnotation("Lorg/tsg/android/api/Annotations$NoTransform;", true);
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

		boolean debug = true;

		if (args.length == 0) {
			throw new RuntimeException("usage: Main [path]");
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
				cv = new TraceClassVisitor(cv, new PrintWriter(System.out, true));
			}

			cv = new Main(cv, details);
			cr.accept(cv, 0);

			// write transformed class out
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			bos.write(cw.toByteArray());
			bos.flush();
			bos.close();
		}
	}
}
