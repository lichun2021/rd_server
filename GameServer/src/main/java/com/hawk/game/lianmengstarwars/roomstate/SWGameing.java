package com.hawk.game.lianmengstarwars.roomstate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.lianmengstarwars.GuildStaticInfo;
import com.hawk.game.lianmengstarwars.ISWWorldPoint;
import com.hawk.game.lianmengstarwars.SWBattleRoom;
import com.hawk.game.lianmengstarwars.SWConst.SWState;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.lianmengstarwars.worldmarch.ISWWorldMarch;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildWar.HPGuildWarCountPush;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.MarchData;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.MarchEventSync;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.chat.ChatParames;

/**
 * 游戏中
 * 
 * @author lwt
 * @date 2018年11月15日
 */
public class SWGameing extends ISWBattleRoomState {
	private List<HawkTuple2<Long, Integer>> countMinList = new LinkedList<>();
	private long nextSyncGuildWarCount;

	public SWGameing(SWBattleRoom room) {
		super(room);
		final long startTime = room.getStartTime();
		long endTime = room.getOverTime();
		for (int i = 1; i < 100 && endTime > startTime; i++) {
			endTime -= TimeUnit.MINUTES.toMillis(1);
			if (i == 10 || i == 20 || i == 30) {
				countMinList.add(HawkTuples.tuple(endTime, i));
			}
		}
		Collections.reverse(countMinList);

//		NoticeCfgId nid = getParent().getExtParm().getWarType() ==SWWarType.JUNIOR? NoticeCfgId.SW_180:NoticeCfgId.SW_181;
//		ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.SPECIAL_BROADCAST)
//				.setKey(nid).build();
//		getParent().addWorldBroadcastMsg(parames);
		// int playerCount = room.getPlayerList(SWState.GAMEING).size();
	}

	@Override
	public boolean onTick() {
		long now = HawkTime.getMillisecond();
		if (!countMinList.isEmpty()) {
			HawkTuple2<Long, Integer> tt = countMinList.get(0);
			if (now > tt.first) {
				countMinList.remove(0);
				ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST)
						.setKey(NoticeCfgId.SW_179).addParms(tt.second).build();
				getParent().addWorldBroadcastMsg(parames);
			}
		}

		List<ISWPlayer> plist = getParent().getPlayerList(SWState.GAMEING);
		for (ISWPlayer player : plist) {
			if (player.getCityDefVal() <= 0) {
				int[] xy = getParent().getWorldPointService().randomFreePoint(getParent().randomPoint(), player.getWorldPointRadius());
				if (xy == null) {
					xy = new int[] { player.getX(), player.getY() };
				}
				// 迁城成功
				getParent().doMoveCitySuccess(player, xy);
				player.sendProtocol(HawkProtocol.valueOf(HP.code.MOVE_CITY_NOTIFY_PUSH));
			}
		}
		for (ISWWorldPoint point : getParent().getViewPoints()) {
			try {
				point.onTick();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		long marchTickCost = 0;
		for (ISWWorldMarch march : getParent().getWorldMarchList()) {
			try {
				long marchTickStart = System.currentTimeMillis();
				if (march.getMarchType() != WorldMarchType.SPY && march.getMarchEntity().getArmyCount() == 0) {
					march.onMarchBack();
					march.remove();
					continue;
				}
				march.heartBeats();

				long marchTickEnd = System.currentTimeMillis() - marchTickStart;
				marchTickCost += marchTickEnd;
				if (marchTickCost > 200) {
					break;
				}
			} catch (Exception e) {
				march.onMarchCallback();
				HawkException.catchException(e);
			}
		}

		// if (tickCnt % 2 == 0) {
		List<ISWWorldMarch> readyPush = getParent().getWorldMarchList().stream().filter(ISWWorldMarch::readyToPush).limit(8).collect(Collectors.toList());
		if (!readyPush.isEmpty()) {
			for (ISWPlayer player : plist) {
				MarchEventSync.Builder builder = MarchEventSync.newBuilder();
				builder.setEventType(MarchEvent.MARCH_UPDATE.getNumber());
				for (ISWWorldMarch march : readyPush) {
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
			for (ISWWorldMarch march : readyPush) {
				march.markUnchanged();
			}
		}

		if (now > nextSyncGuildWarCount) {
			nextSyncGuildWarCount = now + 2000;

			for (GuildStaticInfo gstic : getParent().getGuildStatisticMap().values()) {
				if (StringUtils.isEmpty(gstic.getCsServerId())) {
					continue;
				}
				HPGuildWarCountPush.Builder builder = HPGuildWarCountPush.newBuilder();
				builder.setCount(gstic.getCampAGuildWarCount());
				CrossProxy.getInstance().broadcastProtocolV2(gstic.getCsServerId(), gstic.getPlayerIds(),
						HawkProtocol.valueOf(HP.code.PUSH_GUILD_WAR_COUNT_VALUE, builder));
			}

			for (ISWPlayer player : plist) {
				if (!player.isCsPlayer()) {
					player.getPush().syncGuildWarCount();
				}
			}
		}

		return true;
	}

}
