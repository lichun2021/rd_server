package com.hawk.game.lianmengjunyan.roomstate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.hawk.game.lianmengjunyan.ILMJYWorldPoint;
import com.hawk.game.lianmengjunyan.LMJYBattleRoom;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.lianmengjunyan.msg.LMJYQuitRoomMsg.QuitReason;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.lianmengjunyan.player.LMJYPlayer;
import com.hawk.game.lianmengjunyan.player.npc.LMJYNPCPlayer;
import com.hawk.game.lianmengjunyan.worldmarch.ILMJYWorldMarch;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.util.LogUtil;

/** 游戏中
 * 
 * @author lwt
 * @date 2018年11月15日 */
public class LMJYGameing extends ILMJYBattleRoomState {
	private List<HawkTuple2<Long, Integer>> countMinList = new LinkedList<>();

	public LMJYGameing(LMJYBattleRoom room) {
		super(room);
		final long startTime = room.getStartTime();
		long endTime = room.getOverTime();
		for (int i = 1; i < 100 && endTime > startTime; i++) {
			endTime -= TimeUnit.MINUTES.toMillis(1);
			if (i == 5 || i == 10) {
				countMinList.add(HawkTuples.tuple(endTime, i));
			}
		}
		Collections.reverse(countMinList);

		int playerCount = room.getPlayerList(PState.GAMEING).size();
		LogUtil.logLMJYGameState(room.getId(), 0, room.getGuildId(), room.getBattleCfgId(), playerCount, false);
	}

	@Override
	public boolean onTick() {
		long now = HawkTime.getMillisecond();
		if (!countMinList.isEmpty()) {
			HawkTuple2<Long, Integer> tt = countMinList.get(0);
			if (now > tt.first) {
				countMinList.remove(0);
				ChatParames parames = ChatParames.newBuilder()
						.setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST)
						.setKey(NoticeCfgId.LMJY_TIME_LEFT)
						.addParms(tt.second)
						.build();
				getParent().addWorldBroadcastMsg(parames);
			}
		}

		LMJYBattleRoom room = getParent();

		if (now > getParent().getOverTime()) {
			room.setOverState(3);
			room.setState(new LMJYGameOver(room));
			return true;
		}

		if (chackWin()) {
			getParent().setCampAwin(true);
			getParent().setOverState(1);
			room.setState(new LMJYGameOver(room));
			return true;
		}

		List<ILMJYPlayer> plist = getParent().getPlayerList(PState.GAMEING);
		int playerCount = 0;
		for (ILMJYPlayer player : plist) {
			if (player instanceof LMJYPlayer) {
				if (player.getCityDefVal() <= 0) {
					player.getParent().quitWorld(player, QuitReason.FIREOUT);

					ChatParames parames = ChatParames.newBuilder()
							.setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST)
							.setKey(NoticeCfgId.LMJY_FIRE_OUT)
							.addParms(player.getName())
							.build();
					getParent().addWorldBroadcastMsg(parames);
				} else {
					playerCount++;
				}
			}
		}
		if (playerCount == 0) {
			getParent().setOverState(2);
			room.setState(new LMJYGameOver(room));
		}

		for (ILMJYWorldPoint point : room.getViewPoints()) {
			try {
				point.onTick();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		for (ILMJYWorldMarch march : room.getWorldMarchList()) {
			try {
				march.heartBeats();
			} catch (Exception e) {
				march.onMarchCallback();
				HawkException.catchException(e);
			}
		}

		return true;
	}

	private boolean chackWin() {
		int liveCount = 0;
		List<ILMJYPlayer> plist = getParent().getPlayerList(PState.GAMEING);
		for (ILMJYPlayer player : plist) {
			boolean isNpc = player instanceof LMJYNPCPlayer;
			if (!isNpc) {
				continue;
			}
			if (((LMJYNPCPlayer) player).isDead()) {
				continue;
			}
			boolean hasArmyMarch = getParent()
					.getPlayerMarches(player.getId(), WorldMarchType.ASSISTANCE, WorldMarchType.ATTACK_PLAYER, WorldMarchType.MASS_JOIN, WorldMarchType.MASS).size() > 0;
			if (hasArmyMarch) {
				liveCount++;
				continue;
			}
			if (!player.getData().getMarchArmy().isEmpty()) {
				liveCount++;
				continue;
			}
			((LMJYNPCPlayer) player).setDead(true);
			getParent().worldPointUpdate(player);
			getParent().sync();
			if (player.getLastAttacker() != null) {
				player.getLastAttacker().incKillCount();
				ChatParames parames = ChatParames.newBuilder()
						.setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST)
						.setKey(NoticeCfgId.LMJY_KILL_NPC)
						.addParms(player.getLastAttacker().getName())
						.addParms(player.getName())
						.build();
				getParent().addWorldBroadcastMsg(parames);
			}

		}
		return liveCount == 0;
	}

}
