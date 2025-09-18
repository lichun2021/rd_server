package com.hawk.game.module.lianmengyqzz.march.data.local;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.march.data.IYQZZData;

public class YQZZStatisticsData  implements IYQZZData{

	private static final String redisKey = "YQZZ_ACTIVITY_STATISTICS_DATA";
	
	private String serverId;
	
	private int maxRank;
	
	
	public boolean updateMaxRank(int rank){
		if(rank <= 0){
			return false;
		}
		if(this.maxRank > 0 && this.maxRank < rank){
			return false;
		}
		this.maxRank = rank;
		return true;
	}
	
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	
	public String getServerId() {
		return serverId;
	}
	
	public void setMaxRank(int maxRank) {
		this.maxRank = maxRank;
	}
	
	public int getMaxRank() {
		return maxRank;
	}
	

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("serverId", this.serverId);
		obj.put("maxRank", this.maxRank);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			this.serverId = null;
			this.maxRank = 0;
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.serverId = obj.getString("serverId");
		this.maxRank = obj.getIntValue("maxRank");
	}

	
	
	
	@Override
	public void saveRedis() {
		String key = redisKey  + ":" + this.serverId;
		LocalRedis.getInstance().getRedisSession().setString(key, this.serializ());	
		StatisManager.getInstance().incRedisKey(redisKey);
	}
	
	
	public static YQZZStatisticsData loadData(String serverid){
		String key = redisKey  + ":" + serverid;
		String str = LocalRedis.getInstance().getRedisSession().getString(key);
		if(HawkOSOperator.isEmptyString(str)){
			return null;
		}
		YQZZStatisticsData data = new YQZZStatisticsData();
		data.mergeFrom(str);
		return data;
	}
	
}
