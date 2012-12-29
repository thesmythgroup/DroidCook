package org.tsg.android.asm;

public class Maxs {
	private int mStack;
	private int mLocals;

	public void incStack() {
		incStack(1);
	}

	public void incStack(int n) {
		mStack += n;
	}

	public void incLocals() {
		incLocals(1);
	}

	public void incLocals(int n) {
		mLocals += n;
	}

	public void incAll() {
		incAll(1);
	}

	public void incAll(int n) {
		mStack += n;
		mLocals += n;
	}

	public int getStack() {
		return mStack;
	}

	public int getLocals() {
		return mLocals;
	}
}
