package com.codesmyth.droidcook.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.codesmyth.droidcook.api.Action;
import com.codesmyth.droidcook.api.Bundler;
import com.codesmyth.droidcook.test.Wrapper.Zero;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO use Bundler for shared preferences.
 * TODO consider renaming ACTION on Bundler to ID and/or make available as Uri for database query.
 * or rather, CONTENT instead of ID, but needs an authority ...
 * TODO create defaultValues() method that will set every entry in the underlying Bundle to
 * the java language default value for type, e.g. null, 0, etc.
 * such a method would likely get used in conjunction with replacing rows where no original
 * row data should remain but we also want to query what all the keys are (field name being
 * after last period in key).
 * ...
 * maybe just be better to provide a keySet() method returning all the field names
 * ... underlying Bundle has a keySet method ...
 *
 * TODO consider following developments
 * So, @Action is only ever specified on a presenter; never a view.
 * Action bundles are data-sources, reversed; determines how view will be shown.
 * Action bundles can be received from anywhere; no coupling.
 * e.g. async service delivering result, activity restoring instance status.
 * This is the bundle-presenter pattern.
 *
 * TODO consider `void foo();` as valid for @Bundler;
 * maybe this corresponds to some method `@Action public void foo() {}`
 * Calling `Bundler.foo()` would then broadcast action intent `package.foo`
 * Maybe create simple `FooAction` type with static methods for broadcast
 * like `FooAction.localcast(context)`
 *
 * Maybe I should already be creating these types:
 * `@Action public void foo(Bar bar) {}`
 * `FooAction.localcast(context, bar)`
 * This gives precedence to method name of `@Action`
 * This also allows multiple `@Action` methods with same Bundler arg shared
 * but differing behaviors based on which action fired by discriminating method name.
 * This also makes a nice circle, or at-least a two-way street.
 * Seems like `FooAction.localcast(context, bar)` would be preferred
 * Still need to consider ambiguous method names.
 * Can discriminate method name AND args but no good if still generating class `FooAction`
 * across project.
 *
 * TODO generate xml strings of all ACTION values.
 *
 * TODO create a proper example
 */
public class MainActivity extends Activity implements OnClickListener {

  private MainActivityReceiver receiver = new MainActivityReceiver(this);

  ValueToaster value = new ValueToaster();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);
    findViewById(R.id.click_me).setOnClickListener(this);
    findViewById(R.id.throw_exception).setOnClickListener(this);
    receiver.registerLocal(this);
    value.receiver.registerLocal(this);

    new ZeroBundler()
        .setId(0)
        .localcast(this);

    value.setValue(3).localcast(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    receiver.unregisterLocal(this);
    value.receiver.unregisterLocal(this);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.click_me:
      InformBundler b = Inform.bundler().setId(4);
      b.setMessage("Squared ID: " + b.squaredString());
      b.localcast(this);
      break;
    case R.id.throw_exception:
      try {
        throw new Exception("Exception thrown!");
      } catch (Exception e) {
        new InformBundler()
            .setMessage(e.getMessage())
            .localcast(this);
      }
    default:
      break;
    }
  }

  @Action
  public void notify(Inform ev) {
    Toast.makeText(this, ev.message(), Toast.LENGTH_SHORT).show();
  }

  @Action
  public void zero(Zero ev) {
    Toast.makeText(this, "" + ev.id(), Toast.LENGTH_SHORT).show();
  }

  @Bundler
  interface Value {
    int value();

    @Action
    default void action(Value val) {
      Log.d("@@@", "Value.value() is " + val.value());
    }
  }

  AtomicBoolean once = new AtomicBoolean(false);

  class ValueToaster extends ValueBundler {
    ValueReceiver receiver = new ValueReceiver(this);

    @Override
    public void action(Value a) {
      super.action(a);
      Toast.makeText(MainActivity.this, "value() is " + a.value(), Toast.LENGTH_LONG).show();
      if (once.compareAndSet(false, true)) {
        setValue(value() + a.value()).localcast(MainActivity.this);
      }
    }
  }
}
