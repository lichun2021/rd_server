package com.hawk.game.module.dayazhizhan.playerteam.service;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.player.hero.SerializJsonStrAble;

public class DYZZPlayerScore  implements SerializJsonStrAble{

	
	private String playerId;
	
	private int winCount;

	private int lossCount;

	private int mvpCount;

	private int smvpCount;
	
	
	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("1", playerId);
		obj.put("2", winCount);
		obj.put("3", lossCount);
		obj.put("4", mvpCount);
		obj.put("5", smvpCount);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.playerId = obj.getString("1");
		this.winCount = obj.getIntValue("2");
		this.lossCount = obj.getIntValue("3");
		this.mvpCount = obj.getIntValue("4");
		this.smvpCount = obj.getIntValue("5");
		
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getWinCount() {
		return winCount;
	}

	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}

	public int getLossCount() {
		return lossCount;
	}

	public void setLossCount(int lossCount) {
		this.lossCount = lossCount;
	}

	public int getMvpCount() {
		return mvpCount;
	}

	public void setMvpCount(int mvpCount) {
		this.mvpCount = mvpCount;
	}

	public int getSmvpCount() {
		return smvpCount;
	}

	public void setSmvpCount(int smvpCount) {
		this.smvpCount = smvpCount;
	}


	public int getTotalCount(){
		int total = this.getWinCount() + this.getLossCount();
		return total;
	}
	
	
	public int getWinPer(){
		int total = this.getTotalCount();
		int winPer = 0;
		if(total > 0){
			winPer = this.getWinCount() * 100 / total;
		}
		return winPer;
	}
	
	
	
	
	
	
	
}
