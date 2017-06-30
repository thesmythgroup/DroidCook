package com.codesmyth.droidcook.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.codesmyth.droidcook.api.Action;
import com.codesmyth.droidcook.api.Bundler;
import com.codesmyth.droidcook.test.Wrapper.Zero;

/**
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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);
    findViewById(R.id.click_me).setOnClickListener(this);
    findViewById(R.id.throw_exception).setOnClickListener(this);
    receiver.registerLocal(this);

    new ZeroBundler()
        .setId(0)
        .localcast(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    receiver.unregisterLocal(this);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.click_me:
      new InformBundler()
          .setMessage("Inform")
          .localcast(this);
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

  /**
   * ACTION = "package.GoBackAction";
   * GoBackAction.broadcast(context);
   */
  @Action
  public void goBack() {
    // ...
  }

  /**
   * ACTION = "package.GoBackInformAction";
   * GoBackInformAction.broadcast(context, inform);
   *
   * // this builds nice hierarchy; good namespace properties.
   * // but, if Inform.broadcast behavior preserved, then receiver
   * // must match multiple actions to this method. ??? OK ???
   * //
   * // not nice with minor redundancy "Inform" and "inform". meh
   * ACTION = "package.GoBackAction.Inform";
   * GoBackAction.Inform.broadcast(context, inform);
   *
   * // pursuing this style decouples much of the app implementation.
   * // must still address click listeners.
   * // for empty arguments, onClick only needs ACTION string and
   * // could simply broadcast; decoupling further.
   * // for arguments, ??? bundlers could be set on databinding values ???
   *
   * // TODO generate xml strings of all ACTION values.
   */
  @Action
  void goBack(Inform ev) {
    // ...
  }

  @Bundler
  interface Foo {
    int bar();

    /**
     * this is where things get weird ...
     * and pointless ... TODO disallow
     */
    @Action
    void baz();
  }
}
