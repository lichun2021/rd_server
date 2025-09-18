package com.hawk.game.lianmengcyb.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.CYBORGIronCurtainDeviceCfg;
import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.protocol.World.WorldPointType;

/**
 * 铁幕装置
 *
 */
public class CYBORGIronCurtainDevice extends ICYBORGBuilding {

	public CYBORGIronCurtainDevice(CYBORGBattleRoom parent) {
		super(parent);
	}

	public static CYBORGIronCurtainDeviceCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(CYBORGIronCurtainDeviceCfg.class);
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.CYBORG_IRON_CRUTAIN_DIVICE;
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
}
