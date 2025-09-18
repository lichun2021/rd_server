package com.hawk.game.module.plantsoldier.strengthen.instrumentUpgrade;

import java.util.Objects;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.plantsoldier.strengthen.instrumentUpgrade.cfg.PlantInstrumentUpgradeChipCfg;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantInstrumentChip;

public class InstrumentChip implements SerializJsonStrAble {
	private final PlantInstrument parent;
	private int cfgId;

	public InstrumentChip(PlantInstrument plantTech) {
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

	public PlantInstrumentUpgradeChipCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(PlantInstrumentUpgradeChipCfg.class, cfgId);
	}

	public PlantInstrument getParent() {
		return parent;
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public boolean isUnlock() {
		PlantInstrumentUpgradeChipCfg cfg = getCfg();
		if (cfg.getLevel() > 0 || cfg.getFrontStage() == 0) {
			return true;
		}
		return Objects.nonNull(parent.getChipById(cfg.getFrontStage()));
	}

	public PBPlantInstrumentChip toPBObj() {
		return PBPlantInstrumentChip.newBuilder().setCfgId(getCfgId()).setUnlock(isUnlock()).build();
	}

}
