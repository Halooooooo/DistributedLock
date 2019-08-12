"# DistributedLock"

在方法上加入@DistributedLock 即可锁住整个方法 

普通使用 默认会进行重试 
@DistributedLock(key=KEY,LOCK_EXPIRE=60)
public void method


定时任务 锁不释放
@Scheduled(fixedRate = ONE_DAY)
@DistributedLock(key=KEY,LOCK_EXPIRE=23*60*60,UN_RELEASE = true)
public void method

