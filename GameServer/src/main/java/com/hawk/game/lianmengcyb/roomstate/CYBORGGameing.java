package com.hawk.game.lianmengcyb.roomstate;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.lianmengcyb.CYBORGConst.CYBORGState;
import com.hawk.game.lianmengcyb.CYBORGGuildBaseInfo;
import com.hawk.game.lianmengcyb.CYBORGRoomManager.CYBORG_CAMP;
import com.hawk.game.lianmengcyb.CYBORGTickAbleTask;
import com.hawk.game.lianmengcyb.ICYBORGWorldPoint;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldmarch.ICYBORGWorldMarch;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildWar.HPGuildWarCountPush;
import com.hawk.game.protocol.HP;
import com.hawk.game.service.chat.ChatParames;

/**
 * 游戏中
 * 
 * @author lwt
 * @date 2018年11月15日
 */
public class CYBORGGameing extends ICYBORGBattleRoomState {
	private List<HawkTuple2<Long, Integer>> countMinList = new LinkedList<>();
	private long nextSyncGuildWarCount;
	private boolean hotBloodModelNotPush = true;
	private Map<Integer, CYBORGTickAbleTask> taskMap = new HashMap<>();
	
	public CYBORGGameing(CYBORGBattleRoom room) {
		super(room);
		final long startTime = room.getStartTime();
		long endTime = room.getOverTime();
		for (int i = 1; i < 100 && endTime > startTime; i++) {
			endTime -= TimeUnit.MINUTES.toMillis(1);
			if (i == 10) {
				countMinList.add(HawkTuples.tuple(endTime, i));
			}
		}
		Collections.reverse(countMinList);

		ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.CYBORG_124).build();
		getParent().addWorldBroadcastMsg(parames);
		// int playerCount = room.getPlayerList(CYBORGState.GAMEING).size();
	}

	@Override
	public boolean onTick() {
		long now = HawkTime.getMillisecond();
		if (!countMinList.isEmpty()) {
			HawkTuple2<Long, Integer> tt = countMinList.get(0);
			if (now > tt.first) {
				countMinList.remove(0);
				ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.CYBORG_TENMIN_OVER)
						.build();
				getParent().addWorldBroadcastMsg(parames);
			}
		}

		if (hotBloodModelNotPush && getParent().isHotBloodModel()) {
			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.CYBORG_HOT_BLOOD_MOD)
					.build();
			getParent().addWorldBroadcastMsg(parames);
			hotBloodModelNotPush = false;
		}

		if (now > getParent().getOverTime()) {
			getParent().setOverState(3);
			getParent().setState(new CYBORGGameOver(getParent()));
			return true;
		}

		List<ICYBORGPlayer> plist = getParent().getPlayerList(CYBORGState.GAMEING);
		for (ICYBORGPlayer player : plist) {
			if (player.getCityDefVal() <= 0) {
				int[] xy = getParent().randomFreePoint(getParent().randomPoint(), player.getPointType());
				// 迁城成功
				getParent().doMoveCitySuccess(player, xy);
				player.sendProtocol(HawkProtocol.valueOf(HP.code.MOVE_CITY_NOTIFY_PUSH));
			}
		}

		final int threadNum = HawkTaskManager.getInstance().getThreadNum();
		for (ICYBORGWorldPoint point : getParent().getViewPoints()) {
			getTickAbleTask(point.getHashThread(threadNum)).addPoint(point);
		}

		for (ICYBORGWorldMarch march : getParent().getWorldMarchList()) {
			getTickAbleTask(march.getHashThread(threadNum)).addMarch(march);
		}

		for (CYBORGTickAbleTask invoker : taskMap.values()) {
			if (!invoker.isReadyToRace()) {
				invoker.readyToRace();
				HawkTaskManager.getInstance().postTask(invoker, invoker.getThreadNum());
			}

		}

		if (now > nextSyncGuildWarCount) {
			nextSyncGuildWarCount = now + 2000;
			for (CYBORG_CAMP camp : CYBORG_CAMP.values()) {
				CYBORGGuildBaseInfo ginfo = getParent().getCampBase(camp);
				ginfo.campGuildWarCount = getParent().getGuildWarMarch(ginfo.campGuild).size();
			}

			for (ICYBORGPlayer player : plist) {
				if (!player.isCsPlayer()) {
					player.getPush().syncGuildWarCount();
				}
			}

			for (CYBORGGuildBaseInfo campBase : getParent().getBattleCamps()) {
				if (campBase.isCsGuild) {
					HPGuildWarCountPush.Builder builder = HPGuildWarCountPush.newBuilder();
					builder.setCount(campBase.campGuildWarCount);
					CrossProxy.getInstance().broadcastProtocol(campBase.campServerId, campBase.playerIds,
							HawkProtocol.valueOf(HP.code.PUSH_GUILD_WAR_COUNT_VALUE, builder));

				}
			}

		}

		return true;
	}

	public CYBORGTickAbleTask getTickAbleTask(int threadIndex) {
		if (!taskMap.containsKey(threadIndex)) {
			CYBORGTickAbleTask invoker = new CYBORGTickAbleTask();
			invoker.setBattleId(getParent().getId());
			invoker.setThreadNum(threadIndex);
			taskMap.put(threadIndex, invoker);
		}

		return taskMap.get(threadIndex);

	}
}
