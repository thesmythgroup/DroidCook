package com.codesmyth.droidcook.test;

import com.codesmyth.droidcook.api.Event;

@Event
public interface Notify {
	long id();
	String message();
}
