package com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.state;

import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.IDYZZTower;
import com.hawk.game.protocol.DYZZ.DYZZBuildState;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;

public abstract class IDYZZTowerState {
	private final IDYZZTower parent;

	public IDYZZTowerState(IDYZZTower parent) {
		this.parent = parent;
	}

	public abstract boolean onTick();

	public abstract DYZZBuildState getState();

	public abstract String getGuildId();

	public abstract String getGuildTag();

	public abstract int getGuildFlag();

	public IDYZZTower getParent() {
		return parent;
	}

	public void init() {
	}

	public void fillBuilder(WorldPointPB.Builder builder) {
	}

	public void fillDetailBuilder(WorldPointDetailPB.Builder builder) {

	}
}
