package com.hawk.game.module.lianmengfgyl.battleroom.worldpoint;

import com.hawk.game.module.lianmengfgyl.battleroom.FGYLBattleRoom;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager.FGYL_CAMP;

public class FGYLTower extends IFGYLBuilding {
	private long lastTickTime;

	public FGYLTower(FGYLBattleRoom parent) {
		super(parent);
	}

	@Override
	public boolean onTick() {
		super.onTick();
		long currTime = getParent().getCurTimeMil();
		return true;
	}

}
