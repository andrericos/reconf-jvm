package org.blocks4j.reconf.infra.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ReconfExecutors {

    private ReconfExecutors() {

    }

    public static ExecutorService newReconfThreadExecutor(String name) {
        return Executors.newCachedThreadPool(new ThreadFactory() {
            private int threadCount = 0;

            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                thread.setName(String.format("reconf-%s-%s", name, ++threadCount));
                return thread;
            }
        });
    }

}
