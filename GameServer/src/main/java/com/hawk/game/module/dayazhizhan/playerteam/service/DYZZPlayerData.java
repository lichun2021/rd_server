package com.hawk.game.module.dayazhizhan.playerteam.service;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.player.hero.SerializJsonStrAble;

public class DYZZPlayerData implements SerializJsonStrAble{
	
	private int termId;

	private String serverId;
	
	private String playerId;
	
	private String gameId;
	
	private long enterTime;
	
	private boolean midwayQuit;
	
	private long quitTime;
	
	
	public DYZZPlayerData() {
		
	}

	public DYZZPlayerData(int termId,String gameId,DYZZMember data){
		this.termId = termId;
		this.serverId = data.getServerId();
		this.playerId = data.getPlayerId();
		this.gameId = gameId;
		
	}
	
	
	
	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("1", termId);
		obj.put("2", serverId);
		obj.put("3", playerId);
		obj.put("4", gameId);
		obj.put("5", enterTime);
		obj.put("6", midwayQuit);
		obj.put("7", quitTime);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.termId = obj.getIntValue("1");
		this.serverId = obj.getString("2");
		this.playerId = obj.getString("3");
		this.gameId = obj.getString("4");
		this.enterTime = obj.getLongValue("5");
		this.midwayQuit = obj.getBooleanValue("6");
		this.quitTime = obj.getLongValue("7");
	}
	
	
	
	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
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

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public long getEnterTime() {
		return enterTime;
	}

	public void setEnterTime(long enterTime) {
		this.enterTime = enterTime;
	}

	public boolean isMidwayQuit() {
		return midwayQuit;
	}

	public void setMidwayQuit(boolean midwayQuit) {
		this.midwayQuit = midwayQuit;
	}

	public long getQuitTime() {
		return quitTime;
	}

	public void setQuitTime(long quitTime) {
		this.quitTime = quitTime;
	}
	
	
	
	
	
	
}
