package com.codesmyth.droidcook.common.content;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import com.codesmyth.droidcook.common.util.Strings;
import java.util.ArrayList;
import java.util.List;

public final class Query {

  private Query() {
  }
  public static Builder builder() {
    return new Builder();
  }
  static Bundle getBundle(Cursor cur) {
    Bundle row = new Bundle();
    getBundleInto(row, cur);
    return row;
  }

  static void getBundleInto(Bundle out, Cursor cur) {
    for (int j = 0; j < cur.getColumnCount(); j++) {
      String name = cur.getColumnName(j);
      switch (cur.getType(j)) {
        case Cursor.FIELD_TYPE_BLOB:
          out.putByteArray(name, cur.getBlob(j));
          break;
        case Cursor.FIELD_TYPE_FLOAT:
          out.putFloat(name, cur.getFloat(j));
          break;
        case Cursor.FIELD_TYPE_INTEGER:
          out.putInt(name, cur.getInt(j));
          break;
        case Cursor.FIELD_TYPE_STRING:
          out.putString(name, cur.getString(j));
          break;
        case Cursor.FIELD_TYPE_NULL:
          break;
        default:
          throw new RuntimeException("Unsupported field type: " + cur.getType(j));
      }
    }
  }

  public static class Builder {

    private String[] mSelect;
    private Uri mFrom;
    private String mWhere;
    private String[] mWhereArgs;
    private String mOrderBy;

    public Builder from(Uri x) {
      mFrom = x;
      return this;
    }

    public Builder select(String... x) {
      mSelect = x;
      return this;
    }

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

    public Builder orderBy(String x) {
      mOrderBy = x;
      return this;
    }

    public Cursor exec(Context context) {
      return context.getContentResolver().query(mFrom, mSelect, mWhere, mWhereArgs, mOrderBy);
    }

    public android.support.v4.content.CursorLoader cursorLoaderCompat(Context context) {
      return new android.support.v4.content.CursorLoader(context, mFrom, mSelect, mWhere,
          mWhereArgs, mOrderBy);
    }

    @TargetApi(11)
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
      return Strings.of(result);
    }

    public ArrayList<String> getStringArrayList(Context context, int columnIndex) {
      ArrayList<String> result = new ArrayList<>();
      Cursor c = null;
      try {
        c = exec(context);
        if (c != null) {
          while (c.moveToNext()) {
            result.add(Strings.of(c.getString(columnIndex)));
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

    public Bundle getBundle(Context context) {
      Bundle result = Bundle.EMPTY;
      Cursor c = null;
      try {
        c = exec(context);
        if (c != null && c.moveToFirst()) {
          result = Query.getBundle(c);
        }
      } finally {
        if (c != null) {
          c.close();
        }
      }
      return result;
    }

    public List<Bundle> getBundleList(Context context) {
      ArrayList<Bundle> result = new ArrayList<>();
      Cursor c = null;
      try {
        c = exec(context);
        if (c != null) {
          while (c.moveToNext()) {
            result.add(Query.getBundle(c));
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
}