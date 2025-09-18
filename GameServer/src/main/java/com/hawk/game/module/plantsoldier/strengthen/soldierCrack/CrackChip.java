package com.hawk.game.module.plantsoldier.strengthen.soldierCrack;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.cfg.PlantSoldierCrackChipCfg;
import com.hawk.game.player.hero.SerializJsonStrAble;

public class CrackChip implements SerializJsonStrAble {
	private final PlantSoldierCrack parent;
	private int cfgId;

	public CrackChip(PlantSoldierCrack plantTech) {
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

	public PlantSoldierCrackChipCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(PlantSoldierCrackChipCfg.class, cfgId);
	}

	public PlantSoldierCrack getParent() {
		return parent;
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

}
