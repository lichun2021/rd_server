package com.hawk.game.module.dayazhizhan.battleroom.roomstate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZConst.DYZZState;
import com.hawk.game.module.dayazhizhan.battleroom.IDYZZWorldPoint;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.IDYZZWorldMarch;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.MarchData;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.MarchEventSync;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointSync;

public abstract class IDYZZBattleRoomState {
	private DYZZBattleRoom room;
	private long nextSyncGuildWarCount;

	public IDYZZBattleRoomState(DYZZBattleRoom room) {
		this.room = room;
	}

	public final DYZZBattleRoom getParent() {
		return room;
	}

	public void init() {
	}

	public abstract boolean onTick();

	public boolean onBattleTick() {
		long now = getParent().getCurTimeMil();

		List<IDYZZPlayer> plist = getParent().getPlayerList(DYZZState.GAMEING);
		for (IDYZZPlayer player : plist) {
			if (player.getCityDefVal() <= 0) {
				int[] xy = getParent().randomFreePoint(getParent().randomPoint(), player.getPointType(), player.getRedis());
				// 迁城成功
				getParent().doMoveCitySuccess(player, xy);
				player.sendProtocol(HawkProtocol.valueOf(HP.code.MOVE_CITY_NOTIFY_PUSH));
			}
		}

		for (IDYZZWorldPoint point : getParent().getViewPoints()) {
			try {
				point.onTick();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		for (IDYZZWorldMarch march : getParent().getWorldMarchList()) {
			try {
				if (march.getMarchType() != WorldMarchType.SPY && march.getMarchEntity().getArmyCount() == 0) {
					march.onMarchBack();
					march.remove();
					continue;
				}
				march.heartBeats();
				// march.checkPushMarchEvent();
			} catch (Exception e) {
				march.onMarchCallback();
				HawkException.catchException(e);
			}
		}

		// if (tickCnt % 2 == 0) {
		List<IDYZZWorldMarch> readyPush = getParent().getWorldMarchList().stream().filter(IDYZZWorldMarch::readyToPush).limit(8).collect(Collectors.toList());
		if (!readyPush.isEmpty()) {
			List<IDYZZPlayer> toPush = new ArrayList<>(plist.size() + 5);
			toPush.addAll(plist);
			toPush.addAll(getParent().getAnchors());
			for (IDYZZPlayer player : toPush) {
				MarchEventSync.Builder builder = MarchEventSync.newBuilder();
				builder.setEventType(MarchEvent.MARCH_UPDATE.getNumber());
				for (IDYZZWorldMarch march : readyPush) {
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
			for (IDYZZWorldMarch march : readyPush) {
				march.markUnchanged();
			}
		}

		if (now > nextSyncGuildWarCount) {
			nextSyncGuildWarCount = now + 2000;
			int campAGuildWarCount = getParent().getGuildWarMarch(getParent().getCampAGuild()).size();
			int campBGuildWarCount = getParent().getGuildWarMarch(getParent().getCampBGuild()).size();
			getParent().setCampAGuildWarCount(campAGuildWarCount);
			getParent().setCampBGuildWarCount(campBGuildWarCount);
			for (IDYZZPlayer player : plist) {
				player.getPush().syncGuildWarCount();
			}
		}

		return true;
	}

	public void enterWorld(IDYZZPlayer player) {
		{
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			for (IDYZZWorldPoint point : room.getViewPoints()) {
				builder.addPoints(point.toBuilder(player));
			}
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			player.sendProtocol(protocol);
		}

		// 自己的行军不走这种同步模式
		MarchEventSync.Builder builder = MarchEventSync.newBuilder();
		builder.setEventType(MarchEvent.MARCH_UPDATE.getNumber());
		for (IDYZZWorldMarch march : room.getWorldMarchList()) {
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
