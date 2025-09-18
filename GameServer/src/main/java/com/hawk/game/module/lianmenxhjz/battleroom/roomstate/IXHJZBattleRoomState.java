package com.hawk.game.module.lianmenxhjz.battleroom.roomstate;

import com.hawk.game.module.lianmenxhjz.battleroom.XHJZBattleRoom;
import com.hawk.game.module.lianmenxhjz.battleroom.player.IXHJZPlayer;

public abstract class IXHJZBattleRoomState {
	private XHJZBattleRoom room;

	public IXHJZBattleRoomState(XHJZBattleRoom room) {
		this.room = room;
	}

	public final XHJZBattleRoom getParent() {
		return room;
	}

	public abstract boolean onTick();

	public void enterWorld(IXHJZPlayer player) {
		
	}

}
