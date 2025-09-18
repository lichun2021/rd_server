package com.hawk.game.module.dayazhizhan.playerteam.season;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.DYZZWar.PBDYZZSeasonState;

public class DYZZSeasonInfo implements SerializJsonStrAble{
	
	private int termId;
	
	private PBDYZZSeasonState state = PBDYZZSeasonState.DYZZ_SEASON_HIDDEN;
	
	
	
	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("termId", termId);
		obj.put("state", state.getNumber());
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.termId = obj.getIntValue("termId");
		this.state = PBDYZZSeasonState.valueOf(obj.getIntValue("state"));
		
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public void setState(PBDYZZSeasonState state) {
		this.state = state;
	}

	public int getTermId() {
		return termId;
	}

	public PBDYZZSeasonState getState() {
		return state;
	}

	
	
	
	

}
