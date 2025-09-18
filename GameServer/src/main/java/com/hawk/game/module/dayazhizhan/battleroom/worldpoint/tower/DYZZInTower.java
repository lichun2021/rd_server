package com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower;

import java.util.List;
import java.util.Optional;

import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.protocol.World;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZInTowerCfg;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.DYZZ.DYZZBuildState;

/**
 *
 */
public class DYZZInTower extends IDYZZTower {

	private long protectedEndTime = Long.MAX_VALUE;

	public DYZZInTower(DYZZBattleRoom parent) {
		super(parent);
	}

	@Override
	public boolean onTick() {
		super.onTick();

		long protectedtime = 0;
		if (getState() == DYZZBuildState.ZHAN_LING && getOnwerCamp() == getBornCamp()) {
			List<DYZZOutTower> outtowers = getParent().getDYZZBuildingByClass(DYZZOutTower.class);
			Optional<DYZZOutTower> outtop = outtowers.stream().filter(t -> t.getBornCamp() == getBornCamp()).filter(t -> t.getOnwerCamp() == getBornCamp()).findAny();
			if (outtop.isPresent()) {
				protectedtime = Long.MAX_VALUE;
			}
		}

		if (protectedtime != protectedEndTime) {
			protectedEndTime = protectedtime;
			getParent().worldPointUpdate(this);
		}

		return true;
	}

	@Override
	public long getProtectedEndTime() {
		return protectedEndTime;
	}

	public static DYZZInTowerCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(DYZZInTowerCfg.class);
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
		int atkAdd = getParent().getInTowerAtkAdd(getGuildId());
		return getCfg().getAtkVal() + atkAdd;
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

	@Override
	public World.WorldPointDetailPB.Builder toDetailBuilder(IDYZZPlayer viewer) {
		World.WorldPointDetailPB.Builder builder = super.toDetailBuilder(viewer);
		int atkAdd = getParent().getInTowerAtkAdd(getGuildId());
		builder.setDyzzInTowerAtkAdd(atkAdd);
		return builder;
	}
}
