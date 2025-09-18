package com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZOutTowerCfg;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.protocol.Const.EffType;

/**
 *
 */
public class DYZZOutTower extends IDYZZTower {

	public DYZZOutTower(DYZZBattleRoom parent) {
		super(parent);
	}

	@Override
	public int destroyCountDownMil(IDYZZPlayer leader) {
		return getCfg().getDestroyCountDown() * 1000;
	}

	public static DYZZOutTowerCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(DYZZOutTowerCfg.class);
	}

	@Override
	public int getControlCountDown() {
		return getCfg().getControlCountDown();
	}

	@Override
	public int getAtkCd() {
		return getCfg().getAtkCd();
	}

	@Override
	public int getAtkVal() {
		return getCfg().getAtkVal();
	}

	@Override
	public int getEffVal(EffType effType) {
		return getCfg().getCollectBuffMap().getOrDefault(effType, 0);
	}

	@Override
	public int getWellAtkVal() {
		return getCfg().getWellAtkVal();
	}

	@Override
	public int getOrderAtkVal() {
		return getCfg().getOrderAtkVal();
	}
}
