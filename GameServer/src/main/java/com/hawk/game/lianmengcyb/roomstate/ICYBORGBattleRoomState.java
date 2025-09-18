package com.hawk.game.lianmengcyb.roomstate;

import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;

public abstract class ICYBORGBattleRoomState {
	private CYBORGBattleRoom room;

	public ICYBORGBattleRoomState(CYBORGBattleRoom room) {
		this.room = room;
	}

	public final CYBORGBattleRoom getParent() {
		return room;
	}

	public abstract boolean onTick();

	public void enterWorld(ICYBORGPlayer player) {
		
	}

}
