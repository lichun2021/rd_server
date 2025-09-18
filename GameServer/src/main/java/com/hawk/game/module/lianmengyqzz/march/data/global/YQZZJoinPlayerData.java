package com.hawk.game.module.lianmengyqzz.march.data.global;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.march.data.IYQZZData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;
import com.hawk.game.player.Player;

public class YQZZJoinPlayerData  implements IYQZZData{

	private static final String redisKey = "YQZZ_ACTIVITY_JOIN_PLAYER_DATA";
	
	private int termId;
	
	private String roomId;
	
	private String serverId;
	
	private String playerId;
	
	private String playerName;
	
	private String playerGuild;
	
	private String playerGuildName;
	
	public YQZZJoinPlayerData() {
	}
	
	public YQZZJoinPlayerData(int termId,String roomId,Player player) {
		this.termId = termId;
		this.roomId = roomId;
		this.serverId = player.getMainServerId();
		this.playerId = player.getId();
		this.playerName = player.getName();
		this.playerGuild = player.getGuildId();
		this.playerGuildName = player.getGuildName();
	}
	
	
	public int getTermId() {
		return termId;
	}
	
	public void setTermId(int termId) {
		this.termId = termId;
	}
	public String getRoomId() {
		return roomId;
	}
	
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	
	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getPlayerGuild() {
		return playerGuild;
	}

	public void setPlayerGuild(String playerGuild) {
		this.playerGuild = playerGuild;
	}

	public String getPlayerGuildName() {
		return playerGuildName;
	}

	public void setPlayerGuildName(String playerGuildName) {
		this.playerGuildName = playerGuildName;
	}

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("termId", this.termId);
		obj.put("roomId", this.roomId);
		obj.put("serverId", this.serverId);
		obj.put("playerId", this.playerId);
		obj.put("playerName", this.playerName);
		obj.put("playerGuild", this.playerGuild);
		obj.put("playerGuildName", this.playerGuildName);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			this.termId = 0;
			this.roomId = null;
			this.serverId = null;
			this.playerId = null;
			this.playerName = null;
			this.playerGuild = null;
			this.playerGuildName = null;
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.termId = obj.getIntValue("termId");
		this.roomId = obj.getString("roomId");
		this.serverId = obj.getString("serverId");
		this.playerId = obj.getString("playerId");
		this.playerName = obj.getString("playerName");
		this.playerGuild = obj.getString("playerGuild");
		this.playerGuildName = obj.getString("playerGuildName");
	}

	
	
	
	@Override
	public void saveRedis() {
		String key = redisKey  + ":" + this.termId+":"+this.playerId;
		RedisProxy.getInstance().getRedisSession().setString(key,this.serializ(),YQZZConst.REDIS_DATA_EXPIRE_TIME);	
		StatisManager.getInstance().incRedisKey(redisKey);
	}
	
	
	public static YQZZJoinPlayerData loadData(int termId,String playerId){
		String key = redisKey  + ":" + termId+":"+playerId;
		StatisManager.getInstance().incRedisKey(redisKey);
		String dataStr = RedisProxy.getInstance().getRedisSession().getString(key,YQZZConst.REDIS_DATA_EXPIRE_TIME);	
		if(!HawkOSOperator.isEmptyString(dataStr)){
			YQZZJoinPlayerData data = new YQZZJoinPlayerData();
			data.mergeFrom(dataStr);
			return data;
		}
		return null;
	}
	

	
}
