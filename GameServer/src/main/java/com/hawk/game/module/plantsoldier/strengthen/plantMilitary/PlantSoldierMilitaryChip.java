package com.hawk.game.module.plantsoldier.strengthen.plantMilitary;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.cfg.PlantSoldierMilitaryChipCfg;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantMilitaryChip;

/**
 * 泰能兵军衔部件
 * @author Golden
 *
 */
public class PlantSoldierMilitaryChip implements SerializJsonStrAble {
	
	private final PlantSoldierMilitary parent;
	
	/**
	 * 配置id
	 */
	private int cfgId;

	public PlantSoldierMilitaryChip(PlantSoldierMilitary plant) {
		this.parent = plant;
	}
	
	@Override
	public String serializ() {
		return cfgId + "";
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		cfgId = Integer.valueOf(serialiedStr);
	}

	public PlantSoldierMilitaryChipCfg getCfg() {
		PlantSoldierMilitaryChipCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierMilitaryChipCfg.class, cfgId);
		if (cfg == null) {
			HawkLog.errPrintln("plant soldier military chip get cfg error, cfgId:{}", cfgId);
		}
		return cfg;
	}

	public PlantSoldierMilitaryChipCfg getNextCfg() {
		int nextCfgId = getCfg().getPostStage();
		if (nextCfgId == 0) {
			return null;
		}
		PlantSoldierMilitaryChipCfg nextCfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierMilitaryChipCfg.class, nextCfgId);
		if (nextCfg == null) {
			HawkLog.errPrintln("plant soldier military chip get nextCfg error, cfgId:{}", cfgId);
		}
		return nextCfg;
	}
	
	public PlantSoldierMilitary getParent() {
		return parent;
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public PBPlantMilitaryChip toPBObj() {
		return PBPlantMilitaryChip.newBuilder().setCfgId(getCfgId()).build();
	}
}
