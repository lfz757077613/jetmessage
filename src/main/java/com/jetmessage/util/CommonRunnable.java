package com.jetmessage.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;

@Slf4j
public class CommonRunnable implements Runnable {
    private final Thread curThread;
    private final Map<String, String> mdcMap;
    private final Runnable runnable;

    public CommonRunnable(Runnable runnable) {
        this.curThread = Thread.currentThread();
        this.mdcMap = MDC.getCopyOfContextMap();
        this.runnable = runnable;
    }

    @Override
    public void run() {
        try {
            if (curThread != Thread.currentThread()) {
                if (mdcMap != null) {
                    MDC.setContextMap(mdcMap);
                }
            }
            runnable.run();
        } catch (Throwable t) {
            log.error("CommonRunnable error", t);
            throw t;
        } finally {
            if (curThread != Thread.currentThread()) {
                if (mdcMap != null) {
                    MDC.clear();
                }
            }
        }
    }
}
