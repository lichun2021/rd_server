package com.hawk.game.lianmengcyb.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.CYBORGChronoSphereCfg;
import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.protocol.World.WorldPointType;

/**
 * 超时空传送器
 *
 */
public class CYBORGChronoSphere extends ICYBORGBuilding {

	public CYBORGChronoSphere(CYBORGBattleRoom parent) {
		super(parent);
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.CYBORG_CHRONO_SPHERE;
	}

	public static CYBORGChronoSphereCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(CYBORGChronoSphereCfg.class);
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
