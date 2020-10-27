package com.codesmyth.droidcook.common.content;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import androidx.annotation.NonNull;

import java.util.ArrayList;

// TODO provide way to configure upsert behavior on insert.
public abstract class ContentStub extends ContentProvider {

    private SQLiteOpenHelper database;

    @NonNull
    public abstract SQLiteOpenHelper getDatabase();

    @NonNull
    public abstract String getTableName(Uri uri);

    @Override
    public boolean onCreate() {
        database = getDatabase();
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return "";
    }

    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        SQLiteDatabase db = database.getWritableDatabase();
        ContentProviderResult[] results;
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
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = database.getReadableDatabase();
        return db.query(getTableName(uri), projection, selection, selectionArgs, null, null, sortOrder);
    }

    // TODO notify content changed
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = database.getWritableDatabase();
        return uri.buildUpon().appendPath(
                Long.valueOf(db.insertOrThrow(uri.getLastPathSegment(), null, values)).toString()).build();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = database.getWritableDatabase();
        return db.delete(uri.getLastPathSegment(), selection, selectionArgs);
    }

    // TODO maybe make upsert happen here for simplicity.
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = database.getWritableDatabase();
        return db.update(uri.getLastPathSegment(), values, selection, selectionArgs);
    }
}
