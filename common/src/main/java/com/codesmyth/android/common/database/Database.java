package com.codesmyth.android.common.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class Database extends SQLiteOpenHelper {

    private final Context mContext;
    private final int mRawId;

    public Database(Context context, String name, int version, int rawId) {
        super(context, name, null, version);
        mContext = context;
        mRawId = rawId;
    }

    private String[] schema() throws IOException {
        InputStream is = mContext.getResources().openRawResource(mRawId);
        InputStreamReader isr = new InputStreamReader(is);
        String schema = CharStreams.toString(isr);
        isr.close();
        is.close();
        return schema.split(";");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            for (String stmt : schema()) {
                if (!stmt.startsWith("--") && !stmt.trim().equals("")) {
                    db.execSQL(stmt);
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
