package com.hawk.game.module.plantfactory.tech;

import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.PlantTechnologyChipCfg;
import com.hawk.game.player.hero.SerializJsonStrAble;

public class TechChip implements SerializJsonStrAble {
	private final PlantTech parent;
	private int cfgId;

	public TechChip(PlantTech plantTech) {
		this.parent = plantTech;
	}

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("cfgId", cfgId);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		cfgId = obj.getIntValue("cfgId");

	}

	public PlantTechnologyChipCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(PlantTechnologyChipCfg.class, cfgId);
	}

	public PlantTech getParent() {
		return parent;
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	
}
