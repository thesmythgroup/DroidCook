package com.codesmyth.droidcook.asm.activity;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import com.codesmyth.droidcook.asm.BaseClassVisitor;
import com.codesmyth.droidcook.asm.Details;

public final class ActivityVisitor extends BaseClassVisitor {

	private static List<String> sOverrides = (Arrays.asList(
		"onCreate"
	));

	public ActivityVisitor(ClassVisitor cv, Details details, File file) {
		super(cv, details, file, sOverrides);
	}

	@Override
	public void onVisitEnd() {
		OnCreate.invoke(getVisitor(), getDetails(), getFile(), overridden("onCreate"));
	}

}
