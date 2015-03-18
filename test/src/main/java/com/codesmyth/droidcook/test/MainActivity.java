package com.codesmyth.droidcook.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.codesmyth.droidcook.api.Receive;
import com.codesmyth.droidcook.PackedReceiver;
import com.codesmyth.droidcook.ReceiverFactory;

public class MainActivity extends Activity
    implements OnClickListener {

  private PackedReceiver mReceiver = ReceiverFactory.makeFor(this);
  private PackedReceiver mSpecial = ReceiverFactory.makeFor(new SpecialEvents());

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);
    findViewById(R.id.click_me).setOnClickListener(this);
    findViewById(R.id.throw_exception).setOnClickListener(this);
    mReceiver.register(this);
    mSpecial.register(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mReceiver.unregister(this);
    mSpecial.unregister(this);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.click_me:
      NotifyEvent.build()
          .message("NotifyEvent")
          .broadcast(this);
      break;
    case R.id.throw_exception:
      try {
        throw new Exception("Exception thrown!");
      } catch (Exception e) {
        Event_ErrorEvent.build()
            .message(e.getMessage())
            .broadcast(this);
      }
    default:
      break;
    }
  }

  @Receive
  public void onNotify(NotifyEvent ev) {
    Toast.makeText(this, ev.message(), Toast.LENGTH_SHORT).show();
  }

  @Receive
  public void onError(ErrorEvent ev) {
    Toast.makeText(this, ev.message(), Toast.LENGTH_SHORT).show();
  }

  public class SpecialEvents {
    @Receive
    public void onNotify(NotifyEvent ev) {
      Toast.makeText(MainActivity.this, "Special! " + ev.message(), Toast.LENGTH_LONG).show();
    }
  }
}
