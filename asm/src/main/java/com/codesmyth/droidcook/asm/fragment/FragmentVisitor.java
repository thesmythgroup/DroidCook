package com.codesmyth.droidcook.asm.fragment;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import com.codesmyth.droidcook.asm.BaseClassVisitor;
import com.codesmyth.droidcook.asm.Details;

public final class FragmentVisitor extends BaseClassVisitor {

	private static List<String> sOverrides = (Arrays.asList(
		"onCreateView",
		"onActivityCreated"
	));

	public FragmentVisitor(ClassVisitor cv, Details details, File file) {
		super(cv, details, file, sOverrides);
	}

	@Override
	public void onVisitEnd() {
		OnCreateView.invoke(getVisitor(), getDetails(), getFile(), overridden("onCreateView"));
		OnActivityCreated.invoke(getVisitor(), getDetails(), overridden("onActivityCreated"));
	}

}
