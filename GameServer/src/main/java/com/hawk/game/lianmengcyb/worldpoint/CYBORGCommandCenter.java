package com.hawk.game.lianmengcyb.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.CYBORGCommandCenterCfg;
import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.protocol.World.WorldPointType;

/**
 * 指挥部
 *
 */
public class CYBORGCommandCenter extends ICYBORGBuilding {

	private int cfgId;
	
	public CYBORGCommandCenter(CYBORGBattleRoom parent) {
		super(parent);
	}

	public CYBORGCommandCenterCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(CYBORGCommandCenterCfg.class, cfgId);
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.CYBORG_COMMAND_CENTER;
	}

	@Override
	public int getControlCountDown() {
		return getCfg().getControlCountDown();
	}

	@Override
	public double getGuildHonorPerSecond() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getCfg().getGuildHonor() * beiShu;
	}

	@Override
	public double getPlayerHonorPerSecond() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getCfg().getHonor() * beiShu;
	}

	@Override
	public double getFirstControlGuildHonor() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getCfg().getFirstControlGuildHonor() * beiShu;
	}

	@Override
	public double getFirstControlPlayerHonor() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getCfg().getFirstControlHonor() * beiShu;
	}

	@Override
	public int getProtectTime() {
		return getCfg().getProtectTime();
	}

	@Override
	public int getCollectArmyMin() {
		return getCfg().getCollectArmyMin();
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}
	
	
}
