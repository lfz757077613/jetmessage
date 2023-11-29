package com.jetmessage;

import com.alibaba.fastjson2.JSON;
import com.jetmessage.conf.StaticConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class Application {
    private static volatile String APPLICATION_PATH;
    private static volatile boolean STARTED;
    private static volatile ConfigurableApplicationContext CONTEXT;

    public static void main(String[] args) {
        try {
            if (TimeZone.getDefault().getRawOffset() != TimeUnit.HOURS.toMillis(8)) {
                System.err.println("default timeZone error, timeZone:" + TimeZone.getDefault());
                return;
            }
            if (Charset.defaultCharset() != StandardCharsets.UTF_8) {
                System.err.println("default charset error, charset:" + Charset.defaultCharset());
                return;
            }
            long start = System.currentTimeMillis();
            APPLICATION_PATH = new ApplicationHome(Application.class).getDir().getCanonicalPath();
            CONTEXT = SpringApplication.run(Application.class, args);
            log.info("start success cost:{} config:{}", System.currentTimeMillis() - start, JSON.toJSONString(CONTEXT.getBean(StaticConfig.class)));
            STARTED = true;
            ReentrantLock lock = new ReentrantLock();
            Condition stopCondition = lock.newCondition();
            lock.lock();
            try {
                SpringApplication.getShutdownHandlers().add(() -> {
                    lock.lock();
                    try {
                        STARTED = false;
                        stopCondition.signal();
                    } finally {
                        lock.unlock();
                    }
                });
                while (STARTED) {
                    stopCondition.awaitUninterruptibly();
                }
            } finally {
                lock.unlock();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
    }

    public static String applicationPath() {
        return APPLICATION_PATH;
    }

    public static boolean isStarted() {
        return STARTED;
    }

    public static <T> T getBean(Class<T> type) {
        return CONTEXT.getBean(type);
    }
}