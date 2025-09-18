package com.hawk.game.module.dayazhizhan.battleroom.module;

import org.hawk.app.HawkObjModule;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;

public abstract class DYZZBattleRoomModule extends HawkObjModule {
	protected DYZZBattleRoom battleRoom;

	public DYZZBattleRoomModule(DYZZBattleRoom appObj) {
		super(appObj);
		this.battleRoom = appObj;
	}
	
	public DYZZBattleRoom getParent(){
		return battleRoom;
	}
}
