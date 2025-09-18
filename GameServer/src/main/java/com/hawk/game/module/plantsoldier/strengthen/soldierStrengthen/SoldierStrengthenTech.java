package com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.cfg.PlantSoldierStrengthenTechCfg;
import com.hawk.game.player.hero.SerializJsonStrAble;

public class SoldierStrengthenTech implements SerializJsonStrAble {
	private final SoldierStrengthen parent;
	private int cfgId;

	public SoldierStrengthenTech(SoldierStrengthen plantTech) {
		this.parent = plantTech;
	}

	@Override
	public String serializ() {
		return cfgId + "";
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		cfgId = Integer.valueOf(serialiedStr);
	}

	public PlantSoldierStrengthenTechCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(PlantSoldierStrengthenTechCfg.class, cfgId);
	}

	public SoldierStrengthen getParent() {
		return parent;
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public int getTechId() {
		return (cfgId - 400000) / 100;
	}

	public int getLevel() {
		return cfgId % 100;
	}

}
