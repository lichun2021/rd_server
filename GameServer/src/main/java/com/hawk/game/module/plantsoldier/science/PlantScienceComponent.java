package com.hawk.game.module.plantsoldier.science;

import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.module.plantsoldier.science.cfg.PlantScienceCfg;
import com.hawk.game.player.hero.SerializJsonStrAble;


public class PlantScienceComponent implements SerializJsonStrAble {
	
	/** 科技ID*/
	private int scienceId;
	
	/** 等级*/
	private int level;
	
	/** 状态 0空闲   1研究中*/
	private int state;
	
	
	
	

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("scienceId", this.scienceId);
		obj.put("level", this.level);
		obj.put("state", this.state);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.scienceId = obj.getIntValue("scienceId");
		this.level = obj.getIntValue("level");
		this.state = obj.getIntValue("state");
	}

	public int getScienceId() {
		return scienceId;
	}

	public void setScienceId(int scienceId) {
		this.scienceId = scienceId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	
	public PlantScienceCfg getPlantScienceCfg(){
		return HawkConfigManager.getInstance().getCombineConfig(PlantScienceCfg.class, this.scienceId,this.level);
	}
	
}
