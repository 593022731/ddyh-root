package com.ddyh.product.service.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by cqry_2016 on 2018/7/4
 * 线程池管理类
 */
public class ThreadPoolManager {
    private static Logger logger = LoggerFactory.getLogger(ThreadPoolManager.class);
    /**
     * 根据cpu的数量动态的配置核心线程数和最大线程数
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    /**
     * 核心线程数 = CPU核心数 + 6
     */
    private static final int CORE_POOL_SIZE = CPU_COUNT + 6;
    /**
     * 线程池最大线程数 = CPU核心数 * 6 + 1
     */
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 6 + 1;
    //非核心线程闲置时超时1s
    private static final int KEEP_ALIVE = 2;


    private ThreadPoolManager() {
    }

    private static class ThreadPoolManagerFactory {
        private final static ThreadPoolManager INSTANCE = new ThreadPoolManager();
    }

    public static ThreadPoolManager getInstance() {
        return ThreadPoolManagerFactory.INSTANCE;
    }

    private ThreadPoolExecutor executor;

    /**
     * corePoolSize:核心线程数
     * maximumPoolSize：线程池所容纳最大线程数(workQueue队列满了之后才开启)
     * keepAliveTime：非核心线程闲置时间超时时长
     * unit：keepAliveTime的单位
     * workQueue：等待队列，存储还未执行的任务
     * threadFactory：线程创建的工厂
     * handler：异常处理机制
     */
    public void execute(Runnable r) {
        if (executor == null) {
            executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                    KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<>(20), new MyThreadFactory("dd-product"), new ThreadPoolExecutor.AbortPolicy());

        }
        executor.execute(r);

    }

    /**
     * 把任务移除等待队列
     *
     * @param r
     */
    public void cancel(Runnable r) {
        if (r != null) {
            executor.getQueue().remove(r);
        }
    }

    static class MyThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        MyThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            if (StringUtils.isEmpty(name)) {
                name = "pool";
            }
            namePrefix = name + POOL_NUMBER.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            t.setUncaughtExceptionHandler(new RewriteUncatchtExceptionHandler());
            if (t.isDaemon()) {
                t.setDaemon(false);
            } else if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    static class RewriteUncatchtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            logger.error("thread error!", e.getMessage());
        }
    }
}
