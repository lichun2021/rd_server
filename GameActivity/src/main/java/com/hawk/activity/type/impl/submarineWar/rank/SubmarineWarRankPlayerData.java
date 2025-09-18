package com.hawk.activity.type.impl.submarineWar.rank;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;

/**
 * @author LENOVO
 *
 */
public class SubmarineWarRankPlayerData {

	private String serverId;
	
	private String playerId;
	
	private String playerName;
	
	private String guildName;
	
	private String guildTag;
	
	
	
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
	
	public String getGuildName() {
		return guildName;
	}
	public void setGuildName(String guildName) {
		this.guildName = guildName;
	}
	
	public String getGuildTag() {
		return guildTag;
	}
	
	public void setGuildTag(String guildTag) {
		this.guildTag = guildTag;
	}
	
	public String serializ(){
		JSONObject obj = new JSONObject();
		if(!HawkOSOperator.isEmptyString(this.serverId)){
			obj.put("serverId", this.serverId);
		}
		if(!HawkOSOperator.isEmptyString(this.playerId)){
			obj.put("playerId", this.playerId);
		}
		if(!HawkOSOperator.isEmptyString(this.playerName)){
			obj.put("playerName", this.playerName);
		}
		if(!HawkOSOperator.isEmptyString(this.guildName)){
			obj.put("guildName", this.guildName);
		}
		if(!HawkOSOperator.isEmptyString(this.guildTag)){
			obj.put("guildTag", this.guildTag);
		}
		return obj.toJSONString();
	}
	
	

	public void mergeFrom(String serialiedStr){
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		if(obj.containsKey("serverId")){
			this.serverId = obj.getString("serverId");
		}
		if(obj.containsKey("playerId")){
			this.playerId = obj.getString("playerId");
		}
		if(obj.containsKey("playerName")){
			this.playerName = obj.getString("playerName");
		}
		if(obj.containsKey("guildName")){
			this.guildName = obj.getString("guildName");
		}
		if(obj.containsKey("guildTag")){
			this.guildTag = obj.getString("guildTag");
		}
	}
	
	
	
}
