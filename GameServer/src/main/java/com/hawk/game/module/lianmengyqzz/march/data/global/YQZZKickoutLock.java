package com.hawk.game.module.lianmengyqzz.march.data.global;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;

public class YQZZKickoutLock {
    private static final String redisKey = "YQZZ_ACTIVITY_KICKOUT_LOCK";
    private static final String field1 = "kickoutServer";

    private int termId;

    private String kickoutServer;

    private int expireTime;

    public YQZZKickoutLock(int termId,String server,int expireTime){
        this.termId = termId;
        this.kickoutServer = server;
        this.expireTime = expireTime;
    }

    public boolean achieveKickLockWithExpireTime(){
        String key = redisKey  + ":" + termId;
        long lock = RedisProxy.getInstance().getRedisSession().hSetNx(key, field1, this.kickoutServer);
        StatisManager.getInstance().incRedisKey(redisKey);
        if (lock > 0) {
            RedisProxy.getInstance().getRedisSession().expire(key, this.expireTime);
            return true;
        }
        return false;
    }
}
