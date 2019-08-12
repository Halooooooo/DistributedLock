package com.halo.lock;

/**
 * @author: Halo
 * @Description: 分布式锁
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component("redislock")
public class RedisLock  {
    @Autowired
    private RedisTemplate redisTemplate;

    private static final int FAILURE_TIME = 500;
    private static final String LOCK_KEY_PREFIX = "DISTRIBUTED_LOCK_";
    private static final Logger logger = LoggerFactory.getLogger(RedisLock.class);


    /**
     *  锁的时间30s，防止死锁
     */
    private final static long LOCK_EXPIRE = 30L;
    /**
     *  默认100ms尝试一次
     */
    private final static long LOCK_TRY_INTERVAL = 100L;
    /**
     *  默认尝试40s
     */
    private final static long LOCK_TRY_TIMEOUT = 40 * 1000L;

    /**
     * @author: Halo
     * @Description: 尝试获取锁
     * @param: key
     * @return boolean
     */
    public boolean tryLock(/*@NotNull*/ String key ) {
        return getLock(key,LOCK_TRY_TIMEOUT,LOCK_TRY_INTERVAL,LOCK_EXPIRE);
    }
    public boolean tryLock(/*@NotNull*/ String key,long lockExpireTime ) {
        return getLock(key,LOCK_TRY_TIMEOUT,LOCK_TRY_INTERVAL,lockExpireTime);
    }
    /**
     * @author: Halo
     * @Description: 尝试获取锁
     * @param: key
     * @return boolean
     */
    public boolean tryLock(/*@NotNull*/ String key,long timeout, long tryInterval, long lockExpireTime ) {
        return getLock(key,timeout,tryInterval,lockExpireTime);
    }

    private boolean getLock(/*@NotNull*/ String key, long timeout, long tryInterval, long lockExpireTime) {
        try {
            long startTime = System.currentTimeMillis();
            if(logger.isDebugEnabled()){
                logger.debug("-------------------------------> thread getLock {}",Thread.currentThread().getName());
            }
            do{
                if (!redisTemplate.hasKey((LOCK_KEY_PREFIX + key))) {
                    Boolean haveKey = (Boolean) redisTemplate.execute((RedisCallback<Boolean>) b->{
                        Boolean aBoolean = b.setNX((LOCK_KEY_PREFIX + key).getBytes(), Thread.currentThread().getName().getBytes());
                        if(aBoolean) {
                            b.expire((LOCK_KEY_PREFIX + key).getBytes(),lockExpireTime);
                        }
                        return aBoolean;
                    });
                    if(haveKey){
                        return true;
                    }
                } else {
                    //存在锁
                }
                if (System.currentTimeMillis() - startTime > timeout) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("--------------》重试锁");
                    }
                    return false;
                }
                Thread.sleep(tryInterval);
            }
            while (true) ;

        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public void unlock(/*@NotNull*/ String project, boolean b) {
        if(b) {
            redisTemplate.execute((RedisCallback) call->call.del((LOCK_KEY_PREFIX + project).getBytes()));
        }
    }
    public void unlock(/*@NotNull*/ String project) {
        redisTemplate.execute((RedisCallback) call->call.del((LOCK_KEY_PREFIX + project).getBytes()));
    }

}