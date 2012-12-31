package org.tsg.android.asm;

public class Maxs {
	private int mStack;
	private int mLocals;

	public int getStack() {
		return mStack;
	}

	public void setStack(int n) {
		if (n > mStack) {
			mStack = n;
		}
	}

	public int getLocals() {
		return mLocals;
	}

	public void setLocals(int n) {
		if (n > mLocals) {
			mLocals = n;
		}
	}
}
