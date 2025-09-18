package com.hawk.game.module.lianmengyqzz.battleroom.roomstate;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.MarchData;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.MarchEventSync;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldPointSync;

public abstract class IYQZZBattleRoomState {
	private YQZZBattleRoom room;

	public IYQZZBattleRoomState(YQZZBattleRoom room) {
		this.room = room;
	}

	public final YQZZBattleRoom getParent() {
		return room;
	}

	public abstract boolean onTick();

	public void enterWorld(IYQZZPlayer player) {
		{
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			for (IYQZZWorldPoint point : room.getViewPoints()) {
				builder.addPoints(point.toBuilder(player));
			}
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC, builder);
			player.sendProtocol(protocol);
		}

		// 自己的行军不走这种同步模式
		MarchEventSync.Builder builder = MarchEventSync.newBuilder();
		builder.setEventType(MarchEvent.MARCH_UPDATE.getNumber());
		for (IYQZZWorldMarch march : room.getWorldMarchList()) {
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
