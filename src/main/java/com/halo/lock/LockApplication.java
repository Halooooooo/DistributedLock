package com.halo.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@Slf4j
@EnableAsync
public class LockApplication {

    public final static long ONE_DAY = 24 * 60 * 60 * 1000L;
    public final static long ONE_HOUR = 60 * 60 * 1000L;
    public final static String KEY = "KEY";

    public static void main(String[] args) {
        SpringApplication.run(LockApplication.class, args);
    }


    @Scheduled(fixedRate = ONE_DAY)
    @DistributedLock(key=KEY,LOCK_EXPIRE=23*60*60,UN_RELEASE = true)
    public void gener() {
        if (log.isDebugEnabled()) {
            log.debug("begin deal gener by thread {}", Thread.currentThread().getName());
        }
    }

    /** Set the ThreadPoolExecutor's core pool size. */
    private final int corePoolSize = 2;
    /** Set the ThreadPoolExecutor's maximum pool size. */
    private final int maxPoolSize = 8;
    /** Set the capacity for the ThreadPoolExecutor's BlockingQueue. */
    private final int queueCapacity = 50;

    private String ThreadNamePrefix = "taskExecutor-";
    @Bean("taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setKeepAliveSeconds(20);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(ThreadNamePrefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
