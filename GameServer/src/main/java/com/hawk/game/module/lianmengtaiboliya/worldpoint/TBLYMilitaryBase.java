package com.hawk.game.module.lianmengtaiboliya.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYMilitaryBaseCfg;
import com.hawk.game.module.lianmengtaiboliya.order.TBLYOrderCollection;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GsConst;

/**
 * = 33; // 军事基地
 *
 */
public class TBLYMilitaryBase extends ITBLYBuilding {

	public TBLYMilitaryBase(TBLYBattleRoom parent) {
		super(parent);
	}

	public static TBLYMilitaryBaseCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(TBLYMilitaryBaseCfg.class);
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.TBLY_MILITARY_BASE;
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
		return getCfg().getGuildHonor() * beiShu * (1 + getParent().getCurBuff530Val(EffType.TBLY530_657) * GsConst.EFF_PER);
	}

	@Override
	public double getPlayerHonorPerSecond() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		if (getShowOrder().containsKey(TBLYOrderCollection.shuangbeijifen)) {
			beiShu *= 2;
		}
		return getCfg().getHonor() * beiShu * (1 + getParent().getCurBuff530Val(EffType.TBLY530_657) * GsConst.EFF_PER);
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
