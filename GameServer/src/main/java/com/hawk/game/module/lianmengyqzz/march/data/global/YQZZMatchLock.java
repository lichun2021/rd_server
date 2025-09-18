package com.hawk.game.module.lianmengyqzz.march.data.global;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;

public class YQZZMatchLock{

	private static final String redisKey = "YQZZ_ACTIVITY_MATCH_LOCK";
	private static final String field1 = "matchServer";
	
	private int termId;
	
	private String matchServer;
	
	private int expireTime;
	
	
	public YQZZMatchLock(int termId,String server,int expireTime) {
		this.termId = termId;
		this.matchServer = server;
		this.expireTime = expireTime;
	}

	
	
	
	
	public boolean achieveMatchLockWithExpireTime(){
		String key = redisKey  + ":" + termId;
		long lock = RedisProxy.getInstance().getRedisSession().hSetNx(key, field1, this.matchServer);
		StatisManager.getInstance().incRedisKey(redisKey);
		if (lock > 0) {
			RedisProxy.getInstance().getRedisSession().expire(key, this.expireTime);
			return true;
		}
		return false;
	}
	
	public int getTermId() {
		return termId;
	}
	
	public String getMatchServer() {
		return matchServer;
	}



	


	
	
}
