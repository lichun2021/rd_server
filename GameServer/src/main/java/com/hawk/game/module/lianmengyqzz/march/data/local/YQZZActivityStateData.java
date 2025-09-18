package com.hawk.game.module.lianmengyqzz.march.data.local;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.march.data.IYQZZData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityJoinState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityState;

public class YQZZActivityStateData implements IYQZZData{
	
	
	private static final String redisKey = "YQZZ_ACTIVITY_STATE_DATA";
	
	private String serverId;
	
	private int termId;
	
	private int state;
	
	private int joinGame;
	
	private int saveServerInfo;
	
	
	public YQZZActivityStateData() {
		this.serverId = null;
		this.termId = 0;
		this.state = YQZZActivityState.HIDDEN.getValue();
		this.joinGame = YQZZActivityJoinState.OUT.getValue();
		this.saveServerInfo = 0;
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
	
	public YQZZActivityState getState() {
		return YQZZActivityState.valueOf(this.state);
	}

	public void setState(YQZZActivityState state) {
		this.state = state.getValue();
	}
	
	public YQZZActivityJoinState getJoinGame() {
		return YQZZActivityJoinState.valueOf(this.joinGame);
	}
	
	public void setJoinGame(YQZZActivityJoinState joinGame) {
		this.joinGame = joinGame.getValue();
	}
	
	public int getSaveServerInfo() {
		return saveServerInfo;
	}
	
	public void setSaveServerInfo(int saveServerInfo) {
		this.saveServerInfo = saveServerInfo;
	}
	
	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("serverId", this.serverId);
		obj.put("termId", this.termId);
		obj.put("state", this.state);
		obj.put("joinGame", this.joinGame);
		obj.put("saveServerInfo", this.saveServerInfo);
		return obj.toString();
	}


	@Override
	public void mergeFrom(String serialiedStr) {
		if (HawkOSOperator.isEmptyString(serialiedStr)) {
			this.serverId = null;
			this.termId = 0;
			this.state = 0;
			this.joinGame = 0;
			this.saveServerInfo = 0;
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
		if(obj.containsKey("joinGame")){
			this.joinGame = obj.getIntValue("joinGame");
		}
		if(obj.containsKey("saveServerInfo")){
			this.saveServerInfo = obj.getIntValue("saveServerInfo");
		}
		
	}


	@Override
	public void saveRedis() {
		String key = redisKey  + ":" + this.serverId;
		RedisProxy.getInstance().getRedisSession().setString(key, this.serializ());	
		StatisManager.getInstance().incRedisKey(redisKey);
	}
	
	

	public static YQZZActivityStateData  loadData(String serverId) {
		String key = redisKey  + ":" + serverId;
		String dataStr = RedisProxy.getInstance().getRedisSession().getString(key);
		StatisManager.getInstance().incRedisKey(redisKey);
		if(HawkOSOperator.isEmptyString(dataStr)){
			return null;
		}
		YQZZActivityStateData data = new YQZZActivityStateData();
		data.mergeFrom(dataStr);
		return data;
	}
}
