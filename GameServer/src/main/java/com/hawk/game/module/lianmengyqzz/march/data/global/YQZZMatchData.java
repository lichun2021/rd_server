package com.hawk.game.module.lianmengyqzz.march.data.global;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.march.data.IYQZZData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;

public class YQZZMatchData implements IYQZZData{

	private static final String redisKey = "YQZZ_ACTIVITY_MATCH_DATA";

	private int termId;
	
	private String matchServer;
	
	private long matchTime;
	
	
	
	public YQZZMatchData() {
		this.termId = 0;
		this.matchServer = null;
		this.matchTime = 0;
	}

	public YQZZMatchData(int termId,String matchServer,long matchTime) {
		this.termId = termId;
		this.matchServer = matchServer;
		this.matchTime = matchTime;
	}
	
	public boolean matchFinish(){
		if(!HawkOSOperator.isEmptyString(this.matchServer) &&
				this.matchTime >0 ){
			return true;
		}
		return false;
	}
	
	
	public static YQZZMatchData loadData(int termId){
		String key = redisKey  + ":" + termId;
		String dataStr = RedisProxy.getInstance().getRedisSession()
				.getString(key,YQZZConst.REDIS_DATA_EXPIRE_TIME);
		StatisManager.getInstance().incRedisKey(redisKey);
		if(HawkOSOperator.isEmptyString(dataStr)){
			return null;
		}
		YQZZMatchData data = new YQZZMatchData();
		data.mergeFrom(dataStr);
		return data;
	}
	
	
	public int getTermId() {
		return termId;
	}
	
	public String getMatchServer() {
		return matchServer;
	}
	
	public long getMatchTime() {
		return matchTime;
	}
	

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("termId", this.termId);
		obj.put("matchServer", this.matchServer);
		obj.put("matchTime", this.matchTime);
		return obj.toString();
	}


	@Override
	public void mergeFrom(String serialiedStr) {
		if (HawkOSOperator.isEmptyString(serialiedStr)) {
			this.termId = 0;
			this.matchServer = null;
			this.matchTime = 0;
			return;
		}
		
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		if(obj.containsKey("termId")){
			this.termId = obj.getIntValue("termId");
		}
		if(obj.containsKey("matchServer")){
			this.matchServer = obj.getString("matchServer");
		}
		if(obj.containsKey("matchTime")){
			this.matchTime = obj.getLongValue("matchTime");
		}
	}

	@Override
	public void saveRedis() {
		String key = redisKey  + ":" + termId;
		RedisProxy.getInstance().getRedisSession()
			.setString(key, this.serializ(),YQZZConst.REDIS_DATA_EXPIRE_TIME);	
		StatisManager.getInstance().incRedisKey(redisKey);
	}
	
	
}
