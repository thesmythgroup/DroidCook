package com.codesmyth.droidcook.test;

import com.codesmyth.droidcook.api.Event;

@Event
public abstract class Data {
	public abstract String key();
	public abstract long val();

	public int version() { return 1; }
}
