package com.hawk.game.lianmengstarwars.roomstate;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.lianmengstarwars.ISWWorldPoint;
import com.hawk.game.lianmengstarwars.SWBattleRoom;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.lianmengstarwars.worldmarch.ISWWorldMarch;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.MarchData;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.MarchEventSync;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldPointSync;

public abstract class ISWBattleRoomState {
	private SWBattleRoom room;

	public ISWBattleRoomState(SWBattleRoom room) {
		this.room = room;
	}

	public final SWBattleRoom getParent() {
		return room;
	}

	public abstract boolean onTick();

	public void enterWorld(ISWPlayer player) {
		{
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			for (ISWWorldPoint point : room.getViewPoints()) {
				builder.addPoints(point.toBuilder(player));
			}
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC, builder);
			player.sendProtocol(protocol);
		}

		// 自己的行军不走这种同步模式
		MarchEventSync.Builder builder = MarchEventSync.newBuilder();
		builder.setEventType(MarchEvent.MARCH_UPDATE.getNumber());
		for (ISWWorldMarch march : room.getWorldMarchList()) {
			WorldMarchRelation relation = march.getRelation(player);
			if (relation.equals(WorldMarchRelation.SELF)) {
				continue;
			}
			MarchData.Builder dataBuilder = MarchData.newBuilder();
			dataBuilder.setMarchId(march.getMarchId());
			dataBuilder.setMarchPB(march.toBuilder(relation));
			builder.addMarchData(dataBuilder);
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_EVENT_SYNC_VALUE, builder));
	}

}
