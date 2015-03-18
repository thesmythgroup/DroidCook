package com.codesmyth.droidcook.asm.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Threading {

    private static final Map<String, ExecutorService> sExecs;

    static {
        sExecs = new HashMap<String, ExecutorService>();
        sExecs.put("default", Executors.newCachedThreadPool());
    }

    private Threading() { }

    public static void addExecutorService(String name, ExecutorService es) {
        sExecs.put(name, es);
    }

    public static void execute(Runnable runnable) {
        execute("default", runnable);
    }

    public static void execute(String exec, Runnable runnable) {
        ExecutorService es = sExecs.get(exec);
        if (es == null) {
            throw new RuntimeException("Specified executor does not exist: " + exec);
        }
        es.execute(runnable);
    }

}
