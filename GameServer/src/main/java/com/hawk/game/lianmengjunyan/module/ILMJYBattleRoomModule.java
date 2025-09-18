package com.hawk.game.lianmengjunyan.module;

import org.hawk.app.HawkObjModule;

import com.hawk.game.lianmengjunyan.LMJYBattleRoom;

public abstract class ILMJYBattleRoomModule extends HawkObjModule {
	protected LMJYBattleRoom battleRoom;

	public ILMJYBattleRoomModule(LMJYBattleRoom appObj) {
		super(appObj);
		this.battleRoom = appObj;
	}
	
	public LMJYBattleRoom getParent(){
		return battleRoom;
	}
}
