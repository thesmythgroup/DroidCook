package org.tsg.android.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class Annotations {

	private Annotations() { }

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.CLASS)
	public @interface NoTransform { }

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.CLASS)
	public @interface ContentView {
		int value();
	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.CLASS)
	public @interface ViewById {
		int value() default 0;
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.CLASS)
	public @interface OnCreate { }

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.CLASS)
	public @interface OnClick {
		int[] value();
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.CLASS)
	public @interface UiThread { }

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.CLASS)
	public @interface Background { }
}
