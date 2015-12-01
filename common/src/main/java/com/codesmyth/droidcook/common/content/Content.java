package com.codesmyth.droidcook.common.content;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;

public abstract class Content extends ContentProvider {

  private SQLiteOpenHelper mDatabase;

  @NonNull
  public abstract SQLiteOpenHelper getDatabase();

  @NonNull
  public abstract String getTableName(Uri uri);

  @Override
  public boolean onCreate() {
    mDatabase = getDatabase();
    return true;
  }

  @Override
  public String getType(Uri uri) {
    return "";
  }

  @Override
  public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
    SQLiteDatabase db = mDatabase.getWritableDatabase();
    ContentProviderResult[] results = null;
    db.beginTransaction();
    try {
      results = super.applyBatch(operations);
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
    return results;
  }


  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    SQLiteDatabase db = mDatabase.getReadableDatabase();
    return db.query(getTableName(uri), projection, selection, selectionArgs, null, null, sortOrder);
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    SQLiteDatabase db = mDatabase.getReadableDatabase();
    return uri.buildUpon().appendPath(Long.valueOf(db.insertOrThrow(uri.getLastPathSegment(), null, values)).toString()).build();
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    SQLiteDatabase db = mDatabase.getReadableDatabase();
    return db.delete(uri.getLastPathSegment(), selection, selectionArgs);
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    SQLiteDatabase db = mDatabase.getReadableDatabase();
    return db.update(uri.getLastPathSegment(), values, selection, selectionArgs);
  }
}
