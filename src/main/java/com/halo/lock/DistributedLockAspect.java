package com.halo.lock;

import com.halo.lock.utils.AnnotationResolver;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: Halo
 * @Description:
 */
@Aspect
@Slf4j
@Component
public class DistributedLockAspect {
    @Autowired
    RedisLock redisLock;

    Lock lock = new ReentrantLock();

//    @Async("taskExecutor")
    @Around(value = "@annotation(com.halo.lock.DistributedLock)")
    public Object tryLock(final ProceedingJoinPoint jp) throws Exception {
        JoinPoint jpa = (JoinPoint) jp;
        DistributedLock distributedLock = (DistributedLock) AnnotationResolver.getAnnotation(jpa, DistributedLock.class);
        String key =  distributedLock.key();
        if (StringUtil.isNullOrEmpty(key)) {
            // key 为空。
            return new RuntimeException( "key 为空");
        }
        //NOTE: 如果redis 连接不上 应该进行锁降级，那么需要将tryLock包裹在try cache 中，出现redis的报错，那么应该转化为本地的重入锁
        boolean tryLock = false;
        boolean redisException = false;
        try {
            tryLock = redisLock.tryLock(key, distributedLock.LOCK_TRY_TIMEOUT(), distributedLock.LOCK_TRY_INTERVAL(), distributedLock.LOCK_EXPIRE());
        }catch (Exception e){
            redisException = true;
            if(distributedLock.LOCK_DEGRADE()){
                lock.tryLock(distributedLock.LOCK_TRY_TIMEOUT(), TimeUnit.MICROSECONDS);
            }else{
                throw new RuntimeException(e);
            }
        }
        if(tryLock) {
            // 获取锁成功
            try {
                return jp.proceed();
            } catch (Throwable throwable) {
                throw new RuntimeException("业务异常"+throwable.getMessage(),throwable);
            } finally {
                // 释放锁。
                if(redisException){
                    lock.unlock();
                }
                if(!distributedLock.UN_RELEASE()) {
                    redisLock.unlock(key);
                }
            }
        }
        if(distributedLock.RETRY_FAIL_EXCEPTION()) {
            throw new RuntimeException("锁获取超时");
        }else {
            return null;
        }
    }
}
