package com.hawk.game.module.lianmengtaiboliya.roomstate;

import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;

public abstract class ITBLYBattleRoomState {
	private TBLYBattleRoom room;

	public ITBLYBattleRoomState(TBLYBattleRoom room) {
		this.room = room;
	}

	public final TBLYBattleRoom getParent() {
		return room;
	}

	public abstract boolean onTick();

	public void enterWorld(ITBLYPlayer player) {
		
	}

}
