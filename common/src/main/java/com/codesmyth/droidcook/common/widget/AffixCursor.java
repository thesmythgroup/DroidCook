package com.codesmyth.droidcook.common.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AffixCursor allows attaching of arbitrary elements via types that implement AffixCursor.Affix
 * while maintaining congruency of the underlying cursor during data access.
 * <p/>
 * Any number and/or combination of affixes can be added to a cursor.
 * <pre>
 *   {@code
 *   // important! 0 is reserved for cursor items.
 *   int typeItem = 0;
 *
 *   // the first item in the adapter will have viewType of typeHeader
 *   int typeHeader = 1;
 *   getCursor().addAffix(new AffixCursor.UniquePositionAffix(typeHeader, 0);
 *
 *   // every nth item starting at offset will have viewType of typeAd
 *   int typeAd = 2;
 *   int nth = 7;
 *   int offset = 10;
 *   getCursor().addAffix(new AffixCursor.OffsetNextAffix(typeAd, offset, nth);
 *
 *   // get viewType of a given position to determine how to bind in adapter. This is done
 *   // automatically in provided adapter implementations.
 *   getCursor().getViewType(position);
 *   }
 * </pre>
 *
 * @see com.codesmyth.droidcook.common.widget.FragmentPagerCursorAdapter
 * @see com.codesmyth.droidcook.common.widget.PagerCursorAdapter
 * @see RecyclerCursorAdapterCompat
 */
@SuppressWarnings("unused")
public class AffixCursor implements Serializable {

  public static final int NO_EMPTY_VIEW_TYPE = -1;

  protected Domain mDomain = new Domain();
  // protected for testing mock cursor, but do not attempt to touch this in sub-class!!!
  protected Cursor mCursor;

  private Context mContext;
  private int     mIdColumnIndex;

  private int mEmptyViewType = NO_EMPTY_VIEW_TYPE;

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
    // TODO generate notify events
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

  public void close() {
    if (mCursor != null) {
      mCursor.close();
    }
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

  public void setEmptyViewType(int viewType) {
    if (viewType == 0) {
      throw new IllegalArgumentException("type must not equal 0 as this is implicitly given to default view type.");
    }
    mEmptyViewType = viewType;
  }

  public boolean isNullOrEmpty() {
    return mCursor == null || mCursor.getCount() == 0;
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

  /**
   * The behaviour of this method is largely undefined, use with caution. Given that position arg
   * passes domain test, attempt to find and move to next valid cursor position.
   *
   * @param position
   */
  public void moveToNextValidPositionFrom(int position) {
    if (mDomain.test(position) == Domain.NO_MATCH) {
      throw new IllegalStateException("Argument position should match an Affixable.");
    }
    for (int pos = position; pos < getCount(); pos++) {
      if (mDomain.test(pos) == Domain.NO_MATCH) {
        moveToPositionOrThrow(pos);
        return;
      }
    }
    throw new IllegalStateException("No next valid position was found from position " + position);
  }

  @TargetApi(11)
  public Bundle getBundle() {
    Bundle row = new Bundle();
    getBundleInto(row);
    return row;
  }

  @TargetApi(11)
  public void getBundleInto(Bundle out) {
    for (int j = 0; j < mCursor.getColumnCount(); j++) {
      String name = mCursor.getColumnName(j);
      switch (mCursor.getType(j)) {
      case Cursor.FIELD_TYPE_BLOB:
        out.putByteArray(name, getBlob(j));
        break;
      case Cursor.FIELD_TYPE_FLOAT:
        out.putFloat(name, getFloat(j));
        break;
      case Cursor.FIELD_TYPE_INTEGER:
        out.putInt(name, getInt(j));
        break;
      case Cursor.FIELD_TYPE_STRING:
        out.putString(name, getString(j));
        break;
      case Cursor.FIELD_TYPE_NULL:
        break;
      default:
        throw new RuntimeException("Unsupported field type: " + mCursor.getType(j));
      }
    }
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
    if ((mCursor == null || mCursor.getCount() == 0) && mEmptyViewType != NO_EMPTY_VIEW_TYPE) {
      return mEmptyViewType;
    }
    return mDomain.test(position);
  }

  public int getCount() {
    int count = mCursor == null ? 0 : mCursor.getCount();
    if (count == 0 && mEmptyViewType != NO_EMPTY_VIEW_TYPE) {
      return 1;
    }
    return count + mDomain.n(count);
  }

  public long getId(int position) {
    if (isNullOrEmpty()) {
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
    long id(int position);

    /**
     * Unique view type identifier within a domain, typically identifies viewType defined in adapter.
     */
    int type();

    /**
     * Tests if the given position matches this affix.
     */
    boolean test(int position);

    /**
     * Returns the number of expected occurrences in a given length. Does not account for
     * multiple affixes.
     */
    int n(int length);

    /**
     * Callback to allow an affix to inspect cursor if contents affect outcome of methods.
     */
    void onSwap(Cursor c);
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
      int n = position - mOffset;
      return n >= 0 && n % mNext == 0;
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
      // <= allows for appending an item that trails where-as < contains unique within data set.
      // Still check for zero length and return zero to avoid adding to an empty/null cursor.
      return length == 0 ? 0 : mPosition <= length ? 1 : 0;
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
        Object value;
        if (mColumnType == String.class) {
          value = c.getString(mColumnIndex);
        } else if (mColumnType == Integer.class) {
          value = c.getInt(mColumnIndex);
        } else {
          throw new IllegalStateException(String.format("ColumnType of %s not supported.", mColumnType));
        }
        if (!value.equals(lastValue)) {
          mPositions.add(c.getPosition() + mLength);
          mLength++;
        }
        lastValue = value;
      }
      c.moveToPosition(-1);
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
