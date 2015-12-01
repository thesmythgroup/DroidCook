package com.codesmyth.droidcook.common.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class Database extends SQLiteOpenHelper {

  private final Context mContext;
  private final int     mRawId;

  public Database(Context context) {
    super(context, "db.db", null, 0);
    throw new UnsupportedOperationException("This library class does not implement this constructor.");
  }

  public Database(Context context, String name, int version, int rawId) {
    super(context, name, null, version);
    mContext = context;
    mRawId = rawId;
  }

  protected String[] schema() throws IOException {
    InputStream is = mContext.getResources().openRawResource(mRawId);
    InputStreamReader isr = new InputStreamReader(is);
    String schema = CharStreams.toString(isr);
    isr.close();
    is.close();
    return schema.split("\n");
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.beginTransaction();
    try {
      StringBuilder sb = new StringBuilder();
      for (String stmt : schema()) {
        if (!stmt.startsWith("--") && !stmt.trim().equals("")) {
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
