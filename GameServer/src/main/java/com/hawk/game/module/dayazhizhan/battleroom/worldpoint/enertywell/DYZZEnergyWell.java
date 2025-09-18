package com.hawk.game.module.dayazhizhan.battleroom.worldpoint.enertywell;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZEnergyWellCfg;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.IDYZZBuilding;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.enertywell.state.IDYZZEnergyWellState;
import com.hawk.game.protocol.DYZZ.DYZZBuildState;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;

/**
 * 指挥部
 *
 */
public class DYZZEnergyWell extends IDYZZBuilding {
	private IDYZZEnergyWellState stateObj;

	public DYZZEnergyWell(DYZZBattleRoom parent) {
		super(parent);
	}

	@Override
	public boolean onTick() {
		return stateObj.onTick();
	}
	
	@Override
	public long getProtectedEndTime() {
		return stateObj.getProtectedEndTime();
	}

	public static DYZZEnergyWellCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(DYZZEnergyWellCfg.class);
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.DYZZ_ENERGY_WELL;
	}

	@Override
	public int getControlCountDown() {
		return getCfg().getControlCountDown();
	}

	public IDYZZEnergyWellState getStateObj() {
		return stateObj;
	}

	public void setStateObj(IDYZZEnergyWellState stateObj) {
		this.stateObj = stateObj;
		this.stateObj.init();
		getParent().worldPointUpdate(this);
	}

	@Override
	public WorldPointPB.Builder toBuilder(IDYZZPlayer viewer) {
		WorldPointPB.Builder builder = super.toBuilder(viewer);
		this.stateObj.fillBuilder(builder);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IDYZZPlayer viewer) {
		WorldPointDetailPB.Builder builder = super.toDetailBuilder(viewer);
		this.stateObj.fillDetailBuilder(builder);
		return builder;
	}

	@Override
	public DYZZBuildState getState() {
		return stateObj.getState();
	}

	@Override
	public String getGuildId() {
		if (getLeaderMarch() == null) {
			return "";
		}
		return getLeaderMarch().getParent().getDYZZGuildId();
	}

	@Override
	public String getGuildTag() {
		if (getLeaderMarch() == null) {
			return "";
		}
		return getLeaderMarch().getParent().getGuildTag();
	}

	@Override
	public int getGuildFlag() {
		if (getLeaderMarch() == null) {
			return 0;
		}
		return getLeaderMarch().getParent().getGuildFlag();
	}
}
