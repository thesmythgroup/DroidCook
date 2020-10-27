package com.codesmyth.droidcook.common.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.codesmyth.droidcook.common.IO;
import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("unused")
public abstract class DatabaseStub extends SQLiteOpenHelper {

  private final Context context;
  private final int rawId;

  public DatabaseStub(Context context) {
    super(context, "db.db", null, 0);
    throw new UnsupportedOperationException(
        "This library class does not implement this constructor.");
  }

  public DatabaseStub(Context context, String name, int version, int rawId) {
    super(context, name, null, version);
    this.context = context;
    this.rawId = rawId;
  }

  protected String[] schema() throws IOException {
    InputStream is = context.getResources().openRawResource(rawId);
    byte[] bin = IO.readAll(is);
    String schema = new String(bin);
    is.close();
    return schema.split("\n");
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.beginTransaction();
    try {
      StringBuilder sb = new StringBuilder();
      for (String line : schema()) {
        String stmt = line.trim();
        if (!stmt.startsWith("--") && !stmt.equals("")) {
          sb.append(' ');
          sb.append(stmt);
          if (stmt.endsWith(";")) {
            db.execSQL(sb.toString());
            sb.setLength(0);
          }
        }
      }
      db.setTransactionSuccessful();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      db.endTransaction();
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onCreate(db);
  }
}
