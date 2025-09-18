package com.hawk.game.module.lianmengXianquhx.roomstate;

import com.hawk.game.module.lianmengXianquhx.XQHXBattleRoom;
import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;

public abstract class IXQHXBattleRoomState {
	private XQHXBattleRoom room;

	public IXQHXBattleRoomState(XQHXBattleRoom room) {
		this.room = room;
	}

	public final XQHXBattleRoom getParent() {
		return room;
	}

	public abstract boolean onTick();

	public void enterWorld(IXQHXPlayer player) {
		
	}

}
