package com.codesmyth.android.asm.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 */
public final class Annotations {

	private Annotations() { }

	/**
	 * Causes DroidCook to skip over the class. This annotation is automatically
	 * added once a transformation has taken place on a class file to prevent
	 * future transformations.
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.CLASS)
	public @interface NoTransform { }

	/**
	 * Calls requestFeature
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.CLASS)
	public @interface NoTitle { }

	/**
	 * Call setContentView within generated onCreate method of an Activity
	 * derived class. Uses given resource id, otherwise use class name to
	 * determine id. Examples:
	 *
	 * @ContentView  // R.layout.main_activity
	 * public class MainActivity extends Activity { }
	 *
	 * @ContentView(R.layout.activity_reader)
	 * public class ReaderActivity extends FragmentActivity { }
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.CLASS)
	public @interface ContentView {
		int value() default 0;
	}

	/**
	 * Inject view by given resource id, otherwise use field name to determine
	 * id. Field names are transformed as follows:
	 *
	 * @ViewById
	 * public static TextView sWhack; # R.id.whack
	 *
	 * @ViewById
	 * public TextView mDesc; # R.id.desc
	 *
	 * @ViewById
	 * public ImageView logo; # R.id.logo
	 *
	 * @ViewById(R.id.button)
	 * public Button mAlert; # uses id given
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.CLASS)
	public @interface ViewById {
		int value() default 0;
	}

	/**
	 * Inject intent extra by given name, otherwise use field name to determine
	 * key. Examples:
	 *
	 * @Extra
	 * public String mName; # getStringExtra("name")
	 *
	 * @Extra
	 * public MySerializable mInfo; # getSerializableExtra("info")
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.CLASS)
	public @interface Extra {
		String value() default "@null";
	}

	/**
	 * Inject a resource by the given id, otherwise use field name to determine
	 * id. Examples:
	 *
	 * @Resource
	 * public Animation mFadeIn; # getResources().getAnimation(R.anim.fade_in)
	 *
	 * @Resource(android.R.anim.fade_in)
	 * public Animation mStdFadeIn;
	 *
	 * @Resource
	 * public String mGreeting; # getResources().getString(R.string.greeting)
	 *
	 * @Resource
	 * public int[] mNumArray; # getResources().getIntArray(R.array.num_array)
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.CLASS)
	public @interface Resource {
		int value() default 0;
	}

	/*
	 *
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.CLASS)
	public @interface Prefs {
		String value() default "@null";
	}

	/**
	 * 
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.CLASS)
	public @interface OnCreate { }

	/**
	 * 
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.CLASS)
	public @interface OnCreateView { }

	/**
	 * 
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.CLASS)
	public @interface OnActivityCreated { }

	/**
	 * 
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.CLASS)
	public @interface OnClick {
		int[] value() default 0;
	}

	/**
	 * Not implemented.
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.CLASS)
	public @interface UiThread { }

	/**
	 * Not implemented.
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.CLASS)
	public @interface Background {
		String value() default "@null";
	}
}
