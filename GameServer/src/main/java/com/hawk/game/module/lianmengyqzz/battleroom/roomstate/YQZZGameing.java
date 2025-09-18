package com.hawk.game.module.lianmengyqzz.battleroom.roomstate;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst.YQZZState;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZGuildBaseInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.invoker.YQZZTickAbleTask;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.protocol.GuildWar.HPGuildWarCountPush;
import com.hawk.game.protocol.HP;

/**
 * 游戏中
 * 
 * @author lwt
 * @date 2018年11月15日
 */
public class YQZZGameing extends IYQZZBattleRoomState {
	private List<HawkTuple2<Long, Integer>> countMinList = new LinkedList<>();
	private long nextSyncGuildWarCount;

	private Map<Integer, YQZZTickAbleTask> taskMap = new HashMap<>();

	public YQZZGameing(YQZZBattleRoom room) {
		super(room);
		final long startTime = room.getGameStartTime();
		long endTime = room.getOverTime();
		for (int i = 1; i < 100 && endTime > startTime; i++) {
			endTime -= TimeUnit.MINUTES.toMillis(1);
			if (i == 10) {
				countMinList.add(HawkTuples.tuple(endTime, i));
			}
		}
		Collections.reverse(countMinList);

		// ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.YQZZ_124).build();
		// getParent().addWorldBroadcastMsg(parames);
		// int playerCount = room.getPlayerList(YQZZState.GAMEING).size();
	}

	@Override
	public boolean onTick() {
		long now = HawkTime.getMillisecond();
		if (!countMinList.isEmpty()) {
			HawkTuple2<Long, Integer> tt = countMinList.get(0);
			if (now > tt.first) {
				countMinList.remove(0);
				// ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.YQZZ_TENMIN_OVER)
				// .build();
				// getParent().addWorldBroadcastMsg(parames);
			}
		}

		if (now > getParent().getOverTime()) {
			getParent().setState(new YQZZGameOver(getParent()));
			try {
				// 行军删光
				for (IYQZZWorldMarch march : getParent().getWorldMarchList()) {
					march.onMarchBack();
					march.remove();
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			return true;
		}

		final int threadNum = HawkTaskManager.getInstance().getThreadNum();
		for (IYQZZWorldPoint point : getParent().getViewPoints()) {
			getTickAbleTask(point.getHashThread(threadNum)).addPoint(point);
		}

		for (IYQZZWorldMarch march : getParent().getWorldMarchList()) {
			getTickAbleTask(march.getHashThread()).addMarch(march);
		}

		for (YQZZTickAbleTask invoker : taskMap.values()) {
			if (!invoker.isReadyToRace()) {
				invoker.readyToRace();
				HawkTaskManager.getInstance().postTask(invoker, invoker.getThreadNum());
			}

		}

		if (now > nextSyncGuildWarCount) {
			nextSyncGuildWarCount = now + 2000;
			List<IYQZZPlayer> plist = getParent().getPlayerList(YQZZState.GAMEING);
			for (IYQZZPlayer player : plist) {
				if (!player.isCsPlayer()) {
					player.getPush().syncGuildWarCount();
				}
			}

			for (YQZZGuildBaseInfo campBase : getParent().getBattleCamps()) {
				if (campBase.isCsGuild) {
					HPGuildWarCountPush.Builder builder = HPGuildWarCountPush.newBuilder();
					builder.setCount(campBase.campGuildWarCount);
					CrossProxy.getInstance().broadcastProtocolV2(campBase.campServerId, campBase.playerIds,
							HawkProtocol.valueOf(HP.code.PUSH_GUILD_WAR_COUNT_VALUE, builder));

				}
			}

		}

		return true;
	}

	public YQZZTickAbleTask getTickAbleTask(int threadIndex) {
		if (!taskMap.containsKey(threadIndex)) {
			YQZZTickAbleTask invoker = new YQZZTickAbleTask();
			invoker.setBattleId(getParent().getId());
			invoker.setThreadNum(threadIndex);
			taskMap.put(threadIndex, invoker);
		}

		return taskMap.get(threadIndex);

	}

}
