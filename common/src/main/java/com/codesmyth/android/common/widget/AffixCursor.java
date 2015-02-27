package com.codesmyth.android.common.widget;

import android.content.Context;
import android.database.Cursor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AffixCursor implements Serializable {
  protected Domain mDomain = new Domain();
  // protected for testing mock cursor, but do not attempt to touch this in sub-class!!!
  protected Cursor mCursor;

  private Context mContext;
  private int     mIdColumnIndex;

  public AffixCursor(Context context) {
    mContext = context;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Cursor) {
      return o.equals(mCursor);
    }
    return super.equals(o);
  }

  public boolean swap(Cursor cursor) {
    if (mCursor != cursor) {
      mCursor = cursor;
      if (mCursor != null) {
        mIdColumnIndex = mCursor.getColumnIndexOrThrow("_id");
        for (Affix ax : mDomain.mAffixes) {
          ax.onSwap(mCursor);
        }
      }
      return true;
    }
    return false;
  }

  public Context getContext() {
    return mContext;
  }

  public Domain getDomain() {
    return mDomain;
  }

  public void addAffix(Affix ax) {
    if (ax.type() == 0) {
      throw new IllegalArgumentException("type must not equal 0 as this is implicitly given to default view type.");
    }
    for (Affix ax0 : mDomain.mAffixes) {
      if (ax.type() == ax0.type()) {
        throw new IllegalArgumentException(String.format("type %s already exists in domain.", ax.type()));
      }
    }
    mDomain.mAffixes.add(ax);
  }

  public void clearAffixes() {
    mDomain.mAffixes.clear();
  }

  public boolean moveToNext() {
    return mCursor.moveToNext();
  }

  public void moveToPositionOrThrow(int position) {
    if (!mCursor.moveToPosition(mDomain.convert(position))) {
      throw new IllegalStateException("Unable to move cursor to position " + position);
    }
  }

  public boolean moveToPosition(int position) {
    return mCursor != null && mCursor.moveToPosition(mDomain.convert(position));
  }

  public int getPosition() {
    return mCursor != null ? mCursor.getPosition() : -1;
  }

  public byte[] getBlob(int columnIndex) {
    return mCursor.getBlob(columnIndex);
  }

  public byte[] getBlob(String columnName) {
    return getBlob(getColumnIndexOrThrow(columnName));
  }

  public double getDouble(int columnIndex) {
    return mCursor.getDouble(columnIndex);
  }

  public double getDouble(String columnName) {
    return getDouble(getColumnIndexOrThrow(columnName));
  }

  public float getFloat(int columnIndex) {
    return mCursor.getFloat(columnIndex);
  }

  public float getFloat(String columnName) {
    return getFloat(getColumnIndexOrThrow(columnName));
  }

  public int getInt(int columnIndex) {
    return mCursor.getInt(columnIndex);
  }

  public int getInt(String columnName) {
    return getInt(getColumnIndexOrThrow(columnName));
  }

  public long getLong(int columnIndex) {
    return mCursor.getLong(columnIndex);
  }

  public long getLong(String columnName) {
    return getLong(getColumnIndexOrThrow(columnName));
  }

  public short getShort(int columnIndex) {
    return mCursor.getShort(columnIndex);
  }

  public short getShort(String columnName) {
    return getShort(getColumnIndexOrThrow(columnName));
  }

  public String getString(int columnIndex) {
    return mCursor.getString(columnIndex);
  }

  public String getString(String columnName) {
    return getString(getColumnIndexOrThrow(columnName));
  }

  public boolean isNull(int columnIndex) {
    return mCursor.isNull(columnIndex);
  }

  public boolean isNull(String columnName) {
    return isNull(getColumnIndexOrThrow(columnName));
  }

  public int getColumnIndexOrThrow(String columnName) {
    return mCursor.getColumnIndexOrThrow(columnName);
  }

  public int getViewType(int position) {
    return mDomain.test(position);
  }

  public int getCount() {
    if (mCursor == null) {
      return 0;
    } else {
      int count = mCursor.getCount();
      return count + mDomain.n(count);
    }
  }

  public long getId(int position) {
    if (mCursor == null) {
      return 0L;
    }
    int domainType = mDomain.test(position);
    if (domainType != Domain.NO_MATCH) {
      return mDomain.getAffixByType(domainType).id(position);
    }
    moveToPositionOrThrow(position);
    return mCursor.getLong(mIdColumnIndex);
  }

  /**
   * Given a domain length and position, an affix helps constrain a cursor position to within
   * a larger length set.
   */
  public interface Affix {
    /**
     * Unique identifier within a domain, used by getId() and affects how setHasStableIds on an
     * adapter functions.
     */
    public long id(int position);

    /**
     * Unique view type identifier within a domain, typically identifies viewType defined in adapter.
     */
    public int type();

    /**
     * Tests if the given position matches this affix.
     */
    public boolean test(int position);

    /**
     * Returns the number of expected occurrences in a given length. Does not account for
     * multiple affixes.
     */
    public int n(int length);

    /**
     * Callback to allow an affix to inspect cursor if contents affect outcome of methods.
     */
    public void onSwap(Cursor c);
  }

  public static class OffsetNextAffix implements Affix {
    private int mType;
    private int mOffset;
    private int mNext;

    public OffsetNextAffix(int type, int offset, int next) {
      mType = type;
      mOffset = offset;
      mNext = next;
    }

    @Override
    public long id(int position) {
      return 0L;
    }

    @Override
    public int type() {
      return mType;
    }

    @Override
    public boolean test(int position) {
      return (position - mOffset) % mNext == 0;
    }

    @Override
    public int n(int length) {
      return (int) Math.ceil((length - mOffset) / ((float) mNext));
    }

    @Override
    public void onSwap(Cursor c) {
    }
  }

  public static class UniquePositionAffix implements Affix {
    private int mType;
    private int mPosition;

    public UniquePositionAffix(int type, int position) {
      mType = type;
      mPosition = position;
    }

    @Override
    public long id(int position) {
      return 0L;
    }

    @Override
    public int type() {
      return mType;
    }

    @Override
    public boolean test(int position) {
      return position == mPosition;
    }

    @Override
    public int n(int length) {
      return mPosition < length ? 1 : 0;
    }

    @Override
    public void onSwap(Cursor c) {
    }
  }

  /**
   * Allows column grouping on a cursor. Expects cursor to be ordered by column.
   */
  public static class GroupingAffix implements Affix {
    private int           mType;
    private String        mColumnName;
    private Class         mColumnType;
    private int           mColumnIndex;
    private int           mLength;
    private List<Integer> mPositions;

    public GroupingAffix(int type, String columnName, Class columnType) {
      mType = type;
      mColumnName = columnName;
      mColumnType = columnType;
      mPositions = new ArrayList<>();
    }

    @Override
    public long id(int position) {
      return Long.MAX_VALUE - position;
    }

    @Override
    public int type() {
      return mType;
    }

    @Override
    public boolean test(int position) {
      return mPositions.contains(position);
    }

    @Override
    public int n(int length) {
      for (int i = 0; i < mLength; i++) {
        int pos = mPositions.get(i);
        if (pos >= length) {
          return i;
        }
      }
      return mLength;
    }

    @Override
    public void onSwap(Cursor c) {
      mLength = 0;
      mPositions.clear();
      mColumnIndex = c.getColumnIndexOrThrow(mColumnName);
      Object lastValue = null;
      for (boolean ok = c.moveToFirst(); ok; ok = c.moveToNext()) {
        Object value = null;
        if (mColumnType == String.class) {
          value = c.getString(mColumnIndex);
        } else if (mColumnType == Integer.class) {
          value = c.getInt(mColumnIndex);
        } else {
          throw new IllegalStateException(String.format("ColumnType of %s not supported.", mColumnType));
        }
        if (value != lastValue) {
          mPositions.add(c.getPosition() + mLength);
          mLength++;
        }
        lastValue = value;
      }
      c.moveToPosition(0);
    }
  }

  /**
   * A combination of affixes (logical AND of affixes).
   */
  public static class Domain implements Serializable {
    public static final int NO_MATCH = 0;

    private Map<Affix, Integer> mCache   = new HashMap<>();
    private List<Affix>         mAffixes = new ArrayList<>();

    public int test(int position) {
      for (Affix ax : mAffixes) {
        if (ax.test(position)) {
          return ax.type();
        }
      }
      return NO_MATCH;
    }

    public Affix getAffixByType(int type) {
      for (Affix ax : mAffixes) {
        if (ax.type() == type) {
          return ax;
        }
      }
      throw new IllegalArgumentException(String.format("type %s not part of domain.", type));
    }

    public synchronized int n(int length) {
      mCache.clear();

      int x = 0;
      for (Affix ax : mAffixes) {
        int cur = ax.n(length);
        mCache.put(ax, cur);
        x += cur;
      }

      while (true) {
        int n = 0;
        for (Affix ax : mAffixes) {
          int last = 0;
          if (mCache.containsKey(ax)) {
            last = mCache.get(ax);
          }
          int cur = ax.n(length + x);
          n += (cur - last);
          mCache.put(ax, cur);
        }
        x += n;
        if (n == 0) {
          break;
        }
      }

      return x;
    }

    /**
     * Converts position from inflated length to cursor position.
     */
    public int convert(int position) {
      for (Affix ax : mAffixes) {
        if (ax.test(position)) {
          throw new IllegalArgumentException(String.format("Argument (%s) does not have a real position.", position));
        }
      }
      int newPos = position;
      for (Affix ax : mAffixes) {
        newPos -= ax.n(position);
      }
      return newPos;
    }
  }
}
