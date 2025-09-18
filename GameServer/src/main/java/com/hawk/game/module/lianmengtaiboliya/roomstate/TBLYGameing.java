package com.hawk.game.module.lianmengtaiboliya.roomstate;

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

import com.hawk.game.module.lianmengtaiboliya.ITBLYWorldPoint;
import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.module.lianmengtaiboliya.TBLYTickAbleTask;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.ITBLYWorldMarch;
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
public class TBLYGameing extends ITBLYBattleRoomState {
	private List<HawkTuple2<Long, Integer>> countMinList = new LinkedList<>();
	private long nextSyncGuildWarCount;
	private boolean hotBloodModelNotPush = true;
	private int tickCnt;

	private Map<Integer, TBLYTickAbleTask> taskMap = new HashMap<>();
	
	public TBLYGameing(TBLYBattleRoom room) {
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

		ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.TBLY_124).build();
		getParent().addWorldBroadcastMsg(parames);
		// int playerCount = room.getPlayerList(TBLYState.GAMEING).size();
	}

	@Override
	public boolean onTick() {
		tickCnt++;
		long now = HawkTime.getMillisecond();
		if (!countMinList.isEmpty()) {
			HawkTuple2<Long, Integer> tt = countMinList.get(0);
			if (now > tt.first) {
				countMinList.remove(0);
				ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.TBLY_TENMIN_OVER)
						.build();
				getParent().addWorldBroadcastMsg(parames);
			}
		}

		if (hotBloodModelNotPush && getParent().isHotBloodModel()) {
			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.TBLY_HOT_BLOOD_MOD)
					.build();
			getParent().addWorldBroadcastMsg(parames);
			hotBloodModelNotPush = false;
		}

		if (now > getParent().getOverTime()) {
			getParent().setOverState(3);
			getParent().setState(new TBLYGameOver(getParent()));
			return true;
		}

		List<ITBLYPlayer> plist = getParent().getPlayerList(TBLYState.GAMEING);
		for (ITBLYPlayer player : plist) {
			if (player.getCityDefVal() <= 0) {
				int[] xy = getParent().randomFreePoint(getParent().randomPoint(), player.getPointType());
				// 迁城成功
				getParent().doMoveCitySuccess(player, xy);
				player.sendProtocol(HawkProtocol.valueOf(HP.code.MOVE_CITY_NOTIFY_PUSH));
			}
		}

		final int threadNum = HawkTaskManager.getInstance().getThreadNum();
		for (ITBLYWorldPoint point : getParent().getViewPoints()) {
			getTickAbleTask(point.getHashThread(threadNum)).addPoint(point);
		}

		for (ITBLYWorldMarch march : getParent().getWorldMarchList()) {
			getTickAbleTask(march.getHashThread(threadNum)).addMarch(march);
		}

		for (TBLYTickAbleTask invoker : taskMap.values()) {
			if (!invoker.isReadyToRace()) {
				invoker.readyToRace();
				HawkTaskManager.getInstance().postTask(invoker, invoker.getThreadNum());
			}

		}

		if (now > nextSyncGuildWarCount) {
			nextSyncGuildWarCount = now + 2000;
			HPGuildWarCountPush.Builder builder = HPGuildWarCountPush.newBuilder();
			int campAGuildWarCount = getParent().getGuildWarMarch(getParent().getCampAGuild()).size();
			int campBGuildWarCount = getParent().getGuildWarMarch(getParent().getCampBGuild()).size();
			getParent().setCampAGuildWarCount(campAGuildWarCount);
			getParent().setCampBGuildWarCount(campBGuildWarCount);
			builder.setCount(getParent().getCsGuildWarCount());
			getParent().broadcastCrossProtocol(HawkProtocol.valueOf(HP.code.PUSH_GUILD_WAR_COUNT_VALUE, builder));
			for (ITBLYPlayer player : plist) {
				if (!player.isCsPlayer()) {
					player.getPush().syncGuildWarCount();
				}
			}
		}

		return true;
	}

	public TBLYTickAbleTask getTickAbleTask(int threadIndex) {
		if (!taskMap.containsKey(threadIndex)) {
			TBLYTickAbleTask invoker = new TBLYTickAbleTask();
			invoker.setBattleId(getParent().getId());
			invoker.setThreadNum(threadIndex);
			taskMap.put(threadIndex, invoker);
		}

		return taskMap.get(threadIndex);

	}
}
