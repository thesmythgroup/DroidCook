package com.codesmyth.droidcook.common.content;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import com.google.common.base.Optional;

import java.util.ArrayList;

public final class Query {

  public static Builder builder() { return new Builder(); }

  public static class Builder {

    private String[] mSelect;
    private Uri mFrom;
    private String mWhere;
    private String[] mWhereArgs;
    private String mOrderBy;

    public Builder from(Uri x) { mFrom = x; return this; }

    public Builder select(String... x) { mSelect = x; return this; }

    public Builder where(String x) {
      mWhere = mWhere == null ? x : mWhere + x;
      return this;
    }

    public Builder args(Object... x) {
      ArrayList<String> args = new ArrayList<>(x.length);
      for (Object e : x) {
        args.add(String.valueOf(e));
      }
      mWhereArgs = args.toArray(new String[x.length]);
      return this;
    }

    public Builder orderBy(String x) { mOrderBy = x; return this; }

    public Cursor exec(Context context) {
      return context.getContentResolver().query(mFrom, mSelect, mWhere, mWhereArgs, mOrderBy);
    }

    public CursorLoader cursorLoader(Context context) {
      return new CursorLoader(context, mFrom, mSelect, mWhere, mWhereArgs, mOrderBy);
    }

    public boolean getBoolean(Context context, int columnIndex) {
      boolean result = false;
      Cursor c = null;
      try {
        c = exec(context);
        if (c != null && c.moveToFirst()) {
          result = c.getInt(columnIndex) == 1;
        }
      } finally {
        if (c != null) {
          c.close();
        }
      }
      return result;
    }

    public String getString(Context context, int columnIndex) {
      String result = null;
      Cursor c = null;
      try {
        c = exec(context);
        if (c != null && c.moveToFirst()) {
          result = c.getString(columnIndex);
        }
      } finally {
        if (c != null) {
          c.close();
        }
      }
      return result;
    }

    public Optional<String> getStringOptional(Context context, int columnIndex) {
      String result = null;
      Cursor c = null;
      try {
        c = exec(context);
        if (c != null && c.moveToFirst()) {
          result = c.getString(columnIndex);
        }
      } finally {
        if (c != null) {
          c.close();
        }
      }
      return Optional.fromNullable(result);
    }

    public ArrayList<String> getStringArrayList(Context context, int columnIndex) {
      ArrayList<String> result = new ArrayList<>();
      Cursor c = null;
      try {
        c = exec(context);
        if (c != null) {
          while (c.moveToNext()) {
            result.add(c.getString(columnIndex));
          }
        }
      } finally {
        if (c != null) {
          c.close();
        }
      }
      return result;
    }

    public int getInt(Context context, int columnIndex) {
      int result = -1;
      Cursor c = null;
      try {
        c = exec(context);
        if (c != null && c.moveToFirst()) {
          result = c.getInt(columnIndex);
        }
      } finally {
        if (c != null) {
          c.close();
        }
      }
      return result;
    }

    public Optional<Integer> getIntegerOptional(Context context, int columnIndex) {
      Integer result = null;
      Cursor c = null;
      try {
        c = exec(context);
        if (c != null && c.moveToFirst()) {
          result = c.getInt(columnIndex);
        }
      } finally {
        if (c != null) {
          c.close();
        }
      }
      return Optional.fromNullable(result);
    }

    public ArrayList<Integer> getIntegerArrayList(Context context, int columnIndex) {
      ArrayList<Integer> result = new ArrayList<>();
      Cursor c = null;
      try {
        c = exec(context);
        if (c != null) {
          while (c.moveToNext()) {
            result.add(c.getInt(columnIndex));
          }
        }
      } finally {
        if (c != null) {
          c.close();
        }
      }
      return result;
    }
  }

  private Query() {}
}