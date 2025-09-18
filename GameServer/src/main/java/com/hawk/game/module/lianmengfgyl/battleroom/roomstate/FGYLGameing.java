package com.hawk.game.module.lianmengfgyl.battleroom.roomstate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.hawk.game.module.lianmengfgyl.battleroom.IFGYLWorldPoint;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLBattleRoom;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLConst.FGYLState;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.IFGYLWorldMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLBuildState;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLHeadQuarter;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildWar.HPGuildWarCountPush;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.chat.ChatParames;

/**
 * 游戏中
 * 
 * @author lwt
 * @date 2018年11月15日
 */
public class FGYLGameing extends IFGYLBattleRoomState {
	private List<HawkTuple2<Long, Integer>> countMinList = new LinkedList<>();
	private long nextSyncGuildWarCount;
	private int tickCnt;
	private long buildOpenTwo;
	private long buildOpenFour;
	public FGYLGameing(FGYLBattleRoom room) {
		super(room);
		final long startTime = room.getStartTime();
		long endTime = room.getOverTime();
		for (int i = 1; i < 100 && endTime > startTime; i++) {
			endTime -= TimeUnit.MINUTES.toMillis(1);
			if (i == 5) {
				countMinList.add(HawkTuples.tuple(endTime, i));
			}
		}
		Collections.reverse(countMinList);
//		ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.FGYL_124).build();
//		getParent().addWorldBroadcastMsg(parames);
		// int playerCount = room.getPlayerList(FGYLState.GAMEING).size();
	}

	@Override
	public boolean onTick() {
		tickCnt++;
		long now = HawkTime.getMillisecond();
		if (!countMinList.isEmpty()) {
			HawkTuple2<Long, Integer> tt = countMinList.get(0);
			if (now > tt.first) {
				countMinList.remove(0);
//				ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.FGYL_OVER_FIVE)
//						.build();
//				getParent().addWorldBroadcastMsg(parames);
			}
		}

//		if (now > buildOpenTwo) {
//			buildOpenTwo = Long.MAX_VALUE;
//			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.FGYL_BUILD_TWO)
//					.build();
//			getParent().addWorldBroadcastMsg(parames);
//		}
//		if (now > buildOpenFour) {
//			buildOpenFour = Long.MAX_VALUE;
//			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.FGYL_BUILD_THREE)
//					.build();
//			getParent().addWorldBroadcastMsg(parames);
//		}
		
//		FGYLHeadQuarter quarter = getParent().getFGYLBuildingByClass(FGYLHeadQuarter.class).get(0);
//		if(quarter.getState() == FGYLBuildState.ZHAN_LING){
//			getParent().setOverState(1);
//			getParent().setState(new FGYLGameOver(getParent()));
//			return true;
//		}

		if (now > getParent().getOverTime()) {
			getParent().setOverState(3);
			getParent().setState(new FGYLGameOver(getParent()));
			return true;
		}

		List<IFGYLPlayer> plist = getParent().getPlayerList(FGYLState.GAMEING);
		for(IFGYLPlayer player :plist){
			try {
				player.onTick();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
//		for (IFGYLPlayer player : plist) {
//			if (player.getCityDefVal() <= 0) {
//				int[] xy = getParent().randomFreePoint(getParent().randomPoint(), player.getPointType());
//				// 迁城成功
//				getParent().doMoveCitySuccess(player, xy);
//				player.sendProtocol(HawkProtocol.valueOf(HP.code.MOVE_CITY_NOTIFY_PUSH));
//			}
//		}

		for (IFGYLWorldPoint point : getParent().getViewPoints()) {
			try {
				point.onTick();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		for (IFGYLWorldMarch march : getParent().getWorldMarchList()) {
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

//		// if (tickCnt % 2 == 0) {
//		List<IFGYLWorldMarch> readyPush = getParent().getWorldMarchList().stream().filter(IFGYLWorldMarch::readyToPush).limit(8).collect(Collectors.toList());
//		if (!readyPush.isEmpty() && tickCnt % 2 == 0) {
//			List<IFGYLPlayer> toPush = new ArrayList<>(plist.size() + 5);
//			toPush.addAll(plist);
//			toPush.addAll(getParent().getAnchors());
//			for (IFGYLPlayer player : toPush) {
//				MarchEventSync.Builder builder = MarchEventSync.newBuilder();
//				builder.setEventType(MarchEvent.MARCH_UPDATE.getNumber());
//				for (IFGYLWorldMarch march : readyPush) {
//					WorldMarchRelation relation = march.getRelation(player);
//					if (relation.equals(WorldMarchRelation.SELF)) {
//						continue;
//					}
//					MarchData.Builder dataBuilder = MarchData.newBuilder();
//					dataBuilder.setMarchId(march.getMarchId());
//					dataBuilder.setMarchPB(march.toBuilder(relation));
//					builder.addMarchData(dataBuilder);
//				}
//				player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_EVENT_SYNC_VALUE, builder));
//			}
//			for (IFGYLWorldMarch march : readyPush) {
//				march.markUnchanged();
//			}
//		}

		if (now > nextSyncGuildWarCount) {
			nextSyncGuildWarCount = now + 2000;
			HPGuildWarCountPush.Builder builder = HPGuildWarCountPush.newBuilder();
			int campAGuildWarCount = getParent().getGuildWarMarch(getParent().getBaseInfoA().getGuildId()).size();
			int campBGuildWarCount = getParent().getGuildWarMarch(getParent().getBaseInfoB().getGuildId()).size();
			getParent().setCampAGuildWarCount(campAGuildWarCount);
			getParent().setCampBGuildWarCount(campBGuildWarCount);
			builder.setCount(getParent().getCsGuildWarCount());
			getParent().broadcastCrossProtocol(HawkProtocol.valueOf(HP.code.PUSH_GUILD_WAR_COUNT_VALUE, builder));
			for (IFGYLPlayer player : plist) {
				if (!player.isCsPlayer()) {
					player.getPush().syncGuildWarCount();
				}
			}
		}

		return true;
	}

}
