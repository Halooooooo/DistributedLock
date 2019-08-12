package com.halo.lock;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock  {

    /*
    key 为 DISTRIBUTED_LOCK_+key
     */
    String key();
    /*
        锁的超时时间 30s
     */
    long LOCK_EXPIRE() default 30L;
    /*
        锁的重试时间 100ms
     */
    long LOCK_TRY_INTERVAL() default 100L;

    /*
        锁的重试超时时间 20s 最多允许重试 LOCK_TRY_TIMEOUT / LOCK_TRY_INTERVAL
     */
    long LOCK_TRY_TIMEOUT() default 20 * 1000L;

    /*
    NOTE:
        重试失败后是否抛出异常
        如果不抛出异常则会返回null
     */
    boolean RETRY_FAIL_EXCEPTION() default false;

    /*
        执行完后不对锁进行释放
     */
    boolean UN_RELEASE() default false;

    /*
        redis 重试时，如果redis抛出异常，是否进行锁降级，降为java重入锁。
        锁降级时，UN_RELEASE 属性不生效
     */
    boolean LOCK_DEGRADE() default true;
}
