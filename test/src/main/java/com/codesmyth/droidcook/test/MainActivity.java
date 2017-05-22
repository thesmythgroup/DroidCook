package com.codesmyth.droidcook.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.codesmyth.droidcook.PackedReceiver;
import com.codesmyth.droidcook.ReceiverFactory;
import com.codesmyth.droidcook.api.Receive;
import com.codesmyth.droidcook.test.Wrapper.Zero;

public class MainActivity extends Activity implements OnClickListener {

  private PackedReceiver mReceiver = ReceiverFactory.makeFor(this);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);
    findViewById(R.id.click_me).setOnClickListener(this);
    findViewById(R.id.throw_exception).setOnClickListener(this);
    mReceiver.register(this);

    EventZero.build().id(0).broadcast(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mReceiver.unregister(this);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.click_me:
        EventNotify.build()
            .message("Notify")
            .broadcast(this);
        break;
      case R.id.throw_exception:
        try {
          throw new Exception("Exception thrown!");
        } catch (Exception e) {
          EventNotify.build()
              .message(e.getMessage())
              .broadcast(this);
        }
      default:
        break;
    }
  }

  @Receive
  public void onNotify(Notify ev) {
    Toast.makeText(this, ev.message(), Toast.LENGTH_SHORT).show();
  }

  @Receive
  public void onZero(Zero ev) {
    Toast.makeText(this, ""+ev.id(), Toast.LENGTH_SHORT).show();
  }
}
