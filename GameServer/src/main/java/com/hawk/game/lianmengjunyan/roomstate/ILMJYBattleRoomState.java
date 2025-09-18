package com.hawk.game.lianmengjunyan.roomstate;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.lianmengjunyan.ILMJYWorldPoint;
import com.hawk.game.lianmengjunyan.LMJYBattleRoom;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.lianmengjunyan.worldmarch.ILMJYWorldMarch;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldPointSync;

public abstract class ILMJYBattleRoomState {
	private LMJYBattleRoom room;

	public ILMJYBattleRoomState(LMJYBattleRoom room) {
		this.room = room;
	}

	public final LMJYBattleRoom getParent() {
		return room;
	}

	public abstract boolean onTick();

	public void enterWorld(ILMJYPlayer player) {
		{
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			for (ILMJYWorldPoint point : room.getViewPoints()) {
				builder.addPoints(point.toBuilder(player.getId()));
			}
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			player.sendProtocol(protocol);
		}

		for (ILMJYWorldMarch march : room.getWorldMarchList()) {
			march.notifyMarchEvent(MarchEvent.MARCH_UPDATE, player);
		}

	}

}
