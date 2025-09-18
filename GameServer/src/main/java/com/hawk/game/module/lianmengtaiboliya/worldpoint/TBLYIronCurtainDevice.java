package com.hawk.game.module.lianmengtaiboliya.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYIronCurtainDeviceCfg;
import com.hawk.game.module.lianmengtaiboliya.order.TBLYOrderCollection;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.World.WorldPointType;

/**
 * 铁幕装置
 *
 */
public class TBLYIronCurtainDevice extends ITBLYBuilding {

	public TBLYIronCurtainDevice(TBLYBattleRoom parent) {
		super(parent);
	}

	public static TBLYIronCurtainDeviceCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(TBLYIronCurtainDeviceCfg.class);
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.TBLY_IRON_CRUTAIN_DIVICE;
	}

	@Override
	public int getControlCountDown() {
		return getCfg().getControlCountDown();
	}

	@Override
	public double getGuildHonorPerSecond() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		if (getShowOrder().containsKey(TBLYOrderCollection.shuangbeijifen)) {
			beiShu *= 2;
		}
		return getCfg().getGuildHonor() * beiShu;
	}

	@Override
	public double getPlayerHonorPerSecond() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		if (getShowOrder().containsKey(TBLYOrderCollection.shuangbeijifen)) {
			beiShu *= 2;
		}
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

	@Override
	public int getControlBuff(ITBLYPlayer player, EffType effType) {
		if (underGuildControl(player.getGuildId())) {
			Integer iroVal = TBLYIronCurtainDevice.getCfg().getControleBuffMap().getOrDefault(effType, 0);
			return iroVal;
		}
		return 0;
	}

	@Override
	public int getPointTime() {
		return getCfg().getPointTime();
	}

	@Override
	public double getPointBase() {
		return getCfg().getPointBase();
	}

	@Override
	public double getPointSpeed() {
		return getCfg().getPointSpeed();
	}

	@Override
	public double getPointMax() {
		return getCfg().getPointMax();
	}
}
