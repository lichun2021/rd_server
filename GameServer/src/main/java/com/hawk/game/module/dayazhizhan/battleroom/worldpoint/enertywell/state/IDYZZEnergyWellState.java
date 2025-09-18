package com.hawk.game.module.dayazhizhan.battleroom.worldpoint.enertywell.state;

import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.enertywell.DYZZEnergyWell;
import com.hawk.game.protocol.DYZZ.DYZZBuildState;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;

public abstract class IDYZZEnergyWellState {
	private final DYZZEnergyWell parent;

	public IDYZZEnergyWellState(DYZZEnergyWell parent) {
		this.parent = parent;
	}

	public void init() {
	}

	public abstract boolean onTick();

	public abstract DYZZBuildState getState();

	public DYZZEnergyWell getParent() {
		return parent;
	}

	public void fillBuilder(WorldPointPB.Builder builder) {
	}

	public void fillDetailBuilder(WorldPointDetailPB.Builder builder) {

	}

	public long getProtectedEndTime() {
		// TODO Auto-generated method stub
		return 0;
	}
}
