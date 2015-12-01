package com.codesmyth.droidcook.test.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.test.AndroidTestCase;
import android.test.mock.MockCursor;
import android.view.ViewGroup;
import com.codesmyth.droidcook.common.widget.AffixCursor.GroupingAffix;
import com.codesmyth.droidcook.common.widget.RecyclerCursorAdapterCompat;

import java.util.ArrayList;
import java.util.List;

import static com.codesmyth.droidcook.common.widget.AffixCursor.OffsetNextAffix;
import static com.codesmyth.droidcook.common.widget.AffixCursor.UniquePositionAffix;

public class AffixCursorTest extends AndroidTestCase {

  public static final int TYPE_DEFAULT = 0;
  public static final int TYPE_FIRST   = 1;
  public static final int TYPE_SECOND  = 2;
  public static final int TYPE_THIRD   = 3;
  public static final int TYPE_FOURTH  = 4;

  public void testSwap() {
    MockCursorAdapter adapter = new MockCursorAdapter(getContext(), 5);
    adapter.getCursor().swap(null);
  }

  public void testNullOrEmptyCursor() {
    int cursorLen = 0;
    MockCursorAdapter adapter = new MockCursorAdapter(getContext(), cursorLen, true);
    adapter.getCursor().addAffix(new UniquePositionAffix(TYPE_FIRST, 0));
    adapter.getCursor().addAffix(new OffsetNextAffix(TYPE_SECOND, 0, 10));
    adapter.getCursor().addAffix(new GroupingAffix(TYPE_THIRD, "foo", String.class));

    assertEquals("cursor length", 0, adapter.getItemCount());
  }

  public void testAffixLast() {
    int cursorLen = 5;
    MockCursorAdapter adapter = new MockCursorAdapter(getContext(), cursorLen, true);
    adapter.getCursor().addAffix(new UniquePositionAffix(TYPE_FIRST, 5));

    assertEquals("cursor length", 6, adapter.getItemCount());
  }

  public void testOffset3Next3() {
    int cursorLen = 7;
    MockCursorAdapter adapter = new MockCursorAdapter(getContext(), cursorLen, true);
    adapter.getCursor().addAffix(new OffsetNextAffix(TYPE_FIRST, 3, 3));

    assertEquals("position zero view type", TYPE_DEFAULT, adapter.getItemViewType(0));
  }

  public void testGrouping() {
    int cursorLen = 18;
    MockCursorAdapter adapter = new MockCursorAdapter(getContext(), cursorLen, true);
    adapter.getCursor().addAffix(new GroupingAffix(TYPE_FIRST, "foo", String.class));
    adapter.forceSwap();

    int expectedFakeLen = 22;
    assertEquals("fake length", expectedFakeLen, adapter.getItemCount());

    for (int i = 0; i < expectedFakeLen; i++) {
      int viewType = adapter.getItemViewType(i);
      String msg = "viewType for " + i;
      if (i == 0 || i == 6 || i == 12 || i == 18 || i == 24) {
        assertEquals(msg, TYPE_FIRST, viewType);
      } else {
        assertEquals(msg, TYPE_DEFAULT, viewType);
      }
    }

    int realPos = adapter.getCursor().getDomain().convert(1);
    assertEquals("realPos", 0, realPos);
    realPos = adapter.getCursor().getDomain().convert(7);
    assertEquals("realPos", 5, realPos);
  }

  public void testUniquePosition() {
    int cursorLen = 20;
    MockCursorAdapter adapter = new MockCursorAdapter(getContext(), cursorLen);
    adapter.getCursor().addAffix(new UniquePositionAffix(TYPE_FIRST, 0));

    int expectedFakeLen = 21;
    assertEquals("fake length", expectedFakeLen, adapter.getItemCount());

    for (int i = 0; i < expectedFakeLen; i++) {
      int viewType = adapter.getItemViewType(i);
      if (i == 0) {
        assertEquals("view type", TYPE_FIRST, viewType);
      } else {
        assertEquals("view type", TYPE_DEFAULT, viewType);
      }
    }
  }

  public void testDomain0() {
    int cursorLen = 20;
    MockCursorAdapter adapter = new MockCursorAdapter(getContext(), cursorLen);
    assertEquals("cursor length", cursorLen, adapter.getItemCount());

    for (int i = 0; i < adapter.getItemCount(); i++) {
      String msg = String.format("position %s", i);
      assertEquals("view type", 0, adapter.getItemViewType(i));
      assertEquals(msg, i, adapter.getCursor().getDomain().convert(i));
      adapter.getCursor().moveToPositionOrThrow(i);
      assertEquals("value", i, adapter.getCursor().getInt(0));
      adapter.getItemId(i);
    }
  }

  public void testDomain5Length() {
    int cursorLen = 5;
    MockCursorAdapter adapter = new MockCursorAdapter(getContext(), cursorLen);
    adapter.getCursor().addAffix(new OffsetNextAffix(TYPE_FIRST, 0, 10));
    int expectedFakeLen = 6;
    assertEquals("fake length", expectedFakeLen, adapter.getItemCount());
  }

  public void testDomainPosition() {
    int cursorLen = 20;
    MockCursorAdapter adapter = new MockCursorAdapter(getContext(), cursorLen);
    adapter.getCursor().addAffix(new UniquePositionAffix(TYPE_FIRST, 0));
    adapter.getCursor().addAffix(new OffsetNextAffix(TYPE_SECOND, 2, 10));

    int expectedFakeLen = 24;
    assertEquals("fake length", expectedFakeLen, adapter.getItemCount());

    for (int i = 0; i < expectedFakeLen; i++) {
      int viewType = adapter.getItemViewType(i);
      if (i == 0) {
        assertEquals("view type", TYPE_FIRST, viewType);
      } else if (i == 2 || i == 12 || i == 22) {
        assertEquals("view type", TYPE_SECOND, viewType);
      } else {
        assertEquals("view type", TYPE_DEFAULT, viewType);
      }
    }
  }

  public void testDomain20() {
    int cursorLen = 20;
    MockCursorAdapter adapter = new MockCursorAdapter(getContext(), cursorLen);
    adapter.getCursor().addAffix(new OffsetNextAffix(TYPE_FIRST, 2, 10));
    adapter.getCursor().addAffix(new OffsetNextAffix(TYPE_SECOND, 7, 10));

    // affix at positions (2, 12) and affix at positions (7, 17).
    // 4 affixes plus 20 cursor length == 24
    // but this opens a spot for an additional affix at position (22),
    // so expected length is 25.
    int expectedFakeLen = 25;
    assertEquals("fake length", expectedFakeLen, adapter.getItemCount());

    int cursorPos = -1;
    for (int i = 0; i < expectedFakeLen; i++) {
      String msg = String.format("position %s", i);

      int viewType = adapter.getItemViewType(i);

      int expected = -1;
      if (i == 2 || i == 12 || i == 22) {
        expected = TYPE_FIRST;
      } else if (i == 7 || i == 17) {
        expected = TYPE_SECOND;
      } else {
        expected = TYPE_DEFAULT;
        cursorPos++;
        assertEquals(msg, cursorPos, adapter.getCursor().getDomain().convert(i));
        adapter.getCursor().moveToPositionOrThrow(i);
        assertEquals("value", adapter.getCursor().getDomain().convert(i), adapter.getCursor().getInt(0));
      }

      adapter.getItemId(i);

      assertEquals(msg, expected, viewType);
    }

    assertEquals("iter", cursorLen - 1, cursorPos);
  }

  public void testDomain1000() {
    int cursorLen = 1000;
    MockCursorAdapter adapter = new MockCursorAdapter(getContext(), cursorLen);
    adapter.getCursor().addAffix(new OffsetNextAffix(TYPE_FIRST, 2, 10));
    adapter.getCursor().addAffix(new OffsetNextAffix(TYPE_SECOND, 7, 10));

    // pre-calculated
    int expectedFakeLen = 1250;
    assertEquals("fake length", expectedFakeLen, adapter.getItemCount());

    List<Integer> first = new ArrayList<>();
    for (int i = 2; i < expectedFakeLen; i += 10) {
      first.add(i);
    }

    List<Integer> second = new ArrayList<>();
    for (int i = 7; i < expectedFakeLen; i += 10) {
      second.add(i);
    }

    int cursorPos = -1;
    for (int i = 0; i < expectedFakeLen; i++) {
      String msg = String.format("position %s", i);

      int viewType = adapter.getItemViewType(i);

      int expected = -1;
      if (first.contains(i)) {
        expected = TYPE_FIRST;
      } else if (second.contains(i)) {
        expected = TYPE_SECOND;
      } else {
        expected = TYPE_DEFAULT;
        cursorPos++;
        assertEquals(msg, cursorPos, adapter.getCursor().getDomain().convert(i));
        adapter.getCursor().moveToPositionOrThrow(i);
        assertEquals("value", adapter.getCursor().getDomain().convert(i), adapter.getCursor().getInt(0));
      }

      adapter.getItemId(i);
      assertEquals(msg, expected, viewType);
    }

    assertEquals("iter", cursorLen - 1, cursorPos);
  }

  public static class MockCursorAdapter extends RecyclerCursorAdapterCompat {

    private int     mMockCount;
    private boolean mWithData;

    public MockCursorAdapter(Context context, int mockCount) {
      this(context, mockCount, false);
    }

    public MockCursorAdapter(Context context, int mockCount, boolean withData) {
      super(context);
      mMockCount = mockCount;
      mWithData = withData;
      getCursor().swap(new MockCursorImpl(mockCount, withData));
    }

    public void forceSwap() {
      getCursor().swap(null);
      getCursor().swap(new MockCursorImpl(mMockCount, mWithData));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
      return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
    }
  }

  public static class MockCursorImpl extends MockCursor {
    private int       mMockPosition;
    private int       mMockCount;
    private boolean   mWithData;
    private List<Row> mRows;

    public MockCursorImpl(int mockCount) {
      this(mockCount, false);
    }

    public MockCursorImpl(int mockCount, boolean withData) {
      mMockCount = mockCount;
      mWithData = withData;
      if (withData) {
        mRows = new ArrayList<>();
        String foo = "";
        for (int i = 0; i < mMockCount; i++) {
          if (i % 5 == 0) {
            foo = "foo" + i;
          }
          Row row = new Row(i, foo, "bar" + i);
          mRows.add(row);
        }
      }
    }

    @Override
    public int getCount() {
      return mMockCount;
    }

    @Override
    public boolean moveToPosition(int position) {
      if (position >= 0 && position < mMockCount) {
        mMockPosition = position;
        return true;
      }
      return false;
    }

    @Override
    public boolean moveToFirst() {
      mMockPosition = 0;
      return mMockPosition < getCount();
    }

    @Override
    public boolean moveToNext() {
      mMockPosition++;
      return mMockPosition < getCount();
    }

    @Override
    public int getPosition() {
      return mMockPosition;
    }

    @Override
    public int getInt(int columnIndex) {
      return mMockPosition;
    }

    @Override
    public String getString(int columnIndex) {
      Row row = mRows.get(mMockPosition);
      switch (columnIndex) {
      case 1:
        return row.mFoo;
      case 2:
        return row.mBar;
      }
      throw new IllegalArgumentException("columnIndex " + columnIndex + " for getString not supported.");
    }

    @Override
    public long getLong(int columnIndex) {
      return 0L;
    }

    @Override
    public int getColumnIndexOrThrow(String columnName) {
      if (mWithData) {
        switch (columnName) {
        case "_id":
          return 0;
        case "foo":
          return 1;
        case "bar":
          return 2;
        default:
          throw new IllegalArgumentException("columnName " + columnName + " not supported.");
        }
      }
      return 0;
    }

    private class Row {
      private int    mId;
      private String mFoo;
      private String mBar;

      public Row(int id, String foo, String bar) {
        mId = id;
        mFoo = foo;
        mBar = bar;
      }
    }
  }
}
