package com.hawk.game.module.lianmengfgyl.march.data;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengfgyl.march.service.FGYLConst.FGYLActivityState;

public class FGYLActivityStateData{
	
	
	private String serverId;
	
	private int termId;
	
	private int state;
	
	
	
	public FGYLActivityStateData() {
		this.serverId = null;
		this.termId = 0;
		this.state = FGYLActivityState.HIDDEN.getValue();
	}
	
	
	
	
	public void update(int termId,int state){
		this.termId = termId;
		this.state = state;
	}
	
	
	public String getServerId() {
		return serverId;
	}
	
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	
	
	public int getTermId() {
		return termId;
	}
	
	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	public FGYLActivityState getState() {
		return FGYLActivityState.valueOf(this.state);
	}

	public void setState(FGYLActivityState state) {
		this.state = state.getValue();
	}
	
	
	
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("serverId", this.serverId);
		obj.put("termId", this.termId);
		obj.put("state", this.state);
		return obj.toString();
	}


	public void mergeFrom(String serialiedStr) {
		if (HawkOSOperator.isEmptyString(serialiedStr)) {
			this.serverId = null;
			this.termId = 0;
			this.state = 0;
			return;
		}
		
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		if(obj.containsKey("serverId")){
			this.serverId = obj.getString("serverId");
		}
		if(obj.containsKey("termId")){
			this.termId = obj.getIntValue("termId");
		}
		if(obj.containsKey("state")){
			this.state = obj.getIntValue("state");
		}
		
	}


	public void saveRedis() {
		String key = RedisProxy.FGYL_ACTIVITY_STATE  + ":" + this.serverId;
		RedisProxy.getInstance().getRedisSession().setString(key, this.serializ());	
		StatisManager.getInstance().incRedisKey(key);
	}
	
	

	public static FGYLActivityStateData  loadData(String serverId) {
		String key = RedisProxy.FGYL_ACTIVITY_STATE  + ":" + serverId;
		String dataStr = RedisProxy.getInstance().getRedisSession().getString(key);
		StatisManager.getInstance().incRedisKey(key);
		if(HawkOSOperator.isEmptyString(dataStr)){
			return null;
		}
		FGYLActivityStateData data = new FGYLActivityStateData();
		data.mergeFrom(dataStr);
		return data;
	}
}
