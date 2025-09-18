package com.hawk.game.module.lianmengXianquhx.roomstate;

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

import com.hawk.game.module.lianmengXianquhx.IXQHXWorldPoint;
import com.hawk.game.module.lianmengXianquhx.XQHXBattleRoom;
import com.hawk.game.module.lianmengXianquhx.XQHXConst.XQHXState;
import com.hawk.game.module.lianmengXianquhx.XQHXRoomManager.XQHX_CAMP;
import com.hawk.game.module.lianmengXianquhx.XQHXTickAbleTask;
import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;
import com.hawk.game.module.lianmengXianquhx.worldmarch.IXQHXWorldMarch;
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
public class XQHXGameing extends IXQHXBattleRoomState {
	private List<HawkTuple2<Long, Integer>> countMinList = new LinkedList<>();
	private long nextSyncGuildWarCount;
	private boolean hotBloodModelNotPush = true;
	private int tickCnt;
	private Map<Integer, XQHXTickAbleTask> taskMap = new HashMap<>();
	public XQHXGameing(XQHXBattleRoom room) {
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

		ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.XQHX_124).build();
		getParent().addWorldBroadcastMsg(parames);
		// int playerCount = room.getPlayerList(XQHXState.GAMEING).size();
	}

	@Override
	public boolean onTick() {
		tickCnt++;
		long now = HawkTime.getMillisecond();
		if (!countMinList.isEmpty()) {
			HawkTuple2<Long, Integer> tt = countMinList.get(0);
			if (now > tt.first) {
				countMinList.remove(0);
				ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.XQHX_TENMIN_OVER)
						.build();
				getParent().addWorldBroadcastMsg(parames);
			}
		}

		if (hotBloodModelNotPush && getParent().isHotBloodModel()) {
			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.XQHX_HOT_BLOOD_MOD)
					.build();
			getParent().addWorldBroadcastMsg(parames);
			hotBloodModelNotPush = false;
		} 

		if (now > getParent().getOverTime()) {
			getParent().setOverState(3);
			getParent().setState(new XQHXGameOver(getParent()));
			return true;
		}

		final int threadNum = HawkTaskManager.getInstance().getThreadNum();
		for (IXQHXWorldPoint point : getParent().getViewPoints()) {
			getTickAbleTask(point.getHashThread(threadNum)).addPoint(point);
		}

		for (IXQHXWorldMarch march : getParent().getWorldMarchList()) {
			getTickAbleTask(march.getHashThread(threadNum)).addMarch(march);
		}

		for (XQHXTickAbleTask invoker : taskMap.values()) {
			if (!invoker.isReadyToRace()) {
				invoker.readyToRace();
				HawkTaskManager.getInstance().postTask(invoker, invoker.getThreadNum());
			}

		}


		if (now > nextSyncGuildWarCount) {
			nextSyncGuildWarCount = now + 2000;
			HPGuildWarCountPush.Builder builder = HPGuildWarCountPush.newBuilder();
			int campAGuildWarCount = getParent().getGuildWarMarch(getParent().getCampGuild(XQHX_CAMP.A) ).size();
			int campBGuildWarCount = getParent().getGuildWarMarch(getParent().getCampGuild(XQHX_CAMP.B)).size();
			getParent().getBaseInfoA().campGuildWarCount = campAGuildWarCount;
			getParent().getBaseInfoB().campGuildWarCount = campBGuildWarCount;
			builder.setCount(getParent().getCsGuildWarCount());
			getParent().broadcastCrossProtocol(HawkProtocol.valueOf(HP.code.PUSH_GUILD_WAR_COUNT_VALUE, builder));
			List<IXQHXPlayer> plist = getParent().getPlayerList(XQHXState.GAMEING);
			for (IXQHXPlayer player : plist) {
				if (!player.isCsPlayer()) {
					player.getPush().syncGuildWarCount();
				}
			}
		}

		return true;
	}
	public XQHXTickAbleTask getTickAbleTask(int threadIndex) {
		if (!taskMap.containsKey(threadIndex)) {
			XQHXTickAbleTask invoker = new XQHXTickAbleTask();
			invoker.setBattleId(getParent().getId());
			invoker.setThreadNum(threadIndex);
			taskMap.put(threadIndex, invoker);
		}

		return taskMap.get(threadIndex);

	}
}
