package com.hawk.game.module.dayazhizhan.playerteam.service;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.DYZZWar.PBDYZZWarState;

public class DYZZActivityInfo implements SerializJsonStrAble{
	
	private int termId;
	
	private PBDYZZWarState state = PBDYZZWarState.DYZZ_HIDDEN;
	
	
	
	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("1", state.getNumber());
		obj.put("2", termId);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.state = PBDYZZWarState.valueOf(obj.getIntValue("1"));
		this.termId = obj.getIntValue("2");
		
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public void setState(PBDYZZWarState state) {
		this.state = state;
	}

	public int getTermId() {
		return termId;
	}

	public PBDYZZWarState getState() {
		return state;
	}

	
	
	
	

}
