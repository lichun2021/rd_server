package com.hawk.game.module.lianmengfgyl.battleroom.roomstate;

import com.hawk.game.module.lianmengfgyl.battleroom.FGYLBattleRoom;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;

public abstract class IFGYLBattleRoomState {
	private FGYLBattleRoom room;

	public IFGYLBattleRoomState(FGYLBattleRoom room) {
		this.room = room;
	}

	public final FGYLBattleRoom getParent() {
		return room;
	}

	public abstract boolean onTick();

	public void enterWorld(IFGYLPlayer player) {
		
	}

}
