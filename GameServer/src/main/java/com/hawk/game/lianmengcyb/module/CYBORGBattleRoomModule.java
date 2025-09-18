package com.hawk.game.lianmengcyb.module;

import org.hawk.app.HawkObjModule;

import com.hawk.game.lianmengcyb.CYBORGBattleRoom;

public abstract class CYBORGBattleRoomModule extends HawkObjModule {
	protected CYBORGBattleRoom battleRoom;

	public CYBORGBattleRoomModule(CYBORGBattleRoom appObj) {
		super(appObj);
		this.battleRoom = appObj;
	}
	
	public CYBORGBattleRoom getParent(){
		return battleRoom;
	}
}
