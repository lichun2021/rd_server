package com.hawk.game.module.lianmengyqzz.march.data.global;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.march.data.IYQZZData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;
import com.hawk.game.player.Player;

public class YQZZRoomPlayerData  implements IYQZZData{

	private static final String redisKey = "YQZZ_ACTIVITY_ROOM_PLAYER_DATA";
	
	private int termId;
	
	private String roomId;
	
	private String serverId;
	
	private String playerId;
	
	private String playerName;
	
	private String playerGuild;
	
	private String playerGuildName;
	
	public YQZZRoomPlayerData() {
	}
	
	public YQZZRoomPlayerData(int termId,String roomId,Player player) {
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
		String key = redisKey  + ":" + this.termId+":"+this.roomId;
		RedisProxy.getInstance().getRedisSession().hSet(key, this.playerId, this.serializ(),YQZZConst.REDIS_DATA_EXPIRE_TIME);	
		StatisManager.getInstance().incRedisKey(redisKey);
	}
	
	public static Map<String,YQZZRoomPlayerData> loadAllRoomPlayer(int termId,String roomId){
		String key = redisKey  + ":" + termId+":"+roomId;
		Map<String,YQZZRoomPlayerData> rlt = new HashMap<>();
		Map<String,String> list = RedisProxy.getInstance().getRedisSession()
				.hGetAll(key);
		for(String str : list.values()){
			YQZZRoomPlayerData join = new YQZZRoomPlayerData();
			join.mergeFrom(str);
			rlt.put(join.getPlayerId(), join);
		}
		return rlt;
	}
	
	public static Map<String,YQZZRoomPlayerData> loadAll(int termId,String roomId,List<String> players){
		String key = redisKey  + ":" + termId+":"+roomId;
		Map<String,YQZZRoomPlayerData> rlt = new HashMap<>();
		List<String> list = RedisProxy.getInstance().getRedisSession()
				.hmGet(key, players.toArray(new String[players.size()]));
		for(String str : list){
			YQZZRoomPlayerData join = new YQZZRoomPlayerData();
			join.mergeFrom(str);
			rlt.put(join.getServerId(), join);
		}
		return rlt;
	}
	
}
