package com.hawk.game.lianmengjunyan.roomstate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.hawk.game.invoker.guildBackActivity.GuildBackDropInvoker;
import com.hawk.game.service.ActivityService;
import com.hawk.gamelib.GameConst;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.google.common.base.Objects;
import com.hawk.game.GsApp;
import com.hawk.game.lianmengjunyan.LMJYBattleRoom;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.lianmengjunyan.player.LMJYPlayer;
import com.hawk.game.lianmengjunyan.worldmarch.ILMJYWorldMarch;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Lmjy.PBLMJYGameOver;
import com.hawk.game.protocol.Lmjy.PBLMJYKillInfo;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.LogUtil;

public class LMJYGameOver extends ILMJYBattleRoomState {
	private int delay = 2;
	public LMJYGameOver(LMJYBattleRoom room) {
		super(room);

		int playerCount = room.getPlayerList(PState.GAMEING).size();
		LogUtil.logLMJYGameState(room.getId(), 1, room.getGuildId(), room.getBattleCfgId(), playerCount, room.isCampAwin());
	}

	@Override
	public boolean onTick() {
		if (delay == 2) {
			try {
				List<ILMJYPlayer> playerList = getParent().getPlayerList(PState.GAMEING).stream()
						.filter(p -> p instanceof LMJYPlayer)
						.collect(Collectors.toList());
				for (ILMJYPlayer player : playerList) {
					try {
						// 结算
						jiesuan(player);
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		if (delay-- > 0) {
			return true;
		}
		try {
			// 行军删光
			for (ILMJYWorldMarch march : getParent().getWorldMarchList()) {
				march.remove();
			}
			List<ILMJYPlayer> playerList = getParent().getPlayerList(PState.GAMEING).stream()
					.filter(p -> p instanceof LMJYPlayer)
					.collect(Collectors.toList());
			List<String> playerIds = new ArrayList<>();
			for (ILMJYPlayer player : playerList) {
				try {
					player.getPush().pushGameOver();
					playerIds.add(player.getId());
				} catch (Exception e) {
					player.getData().unLockOriginalData();
					player.setLmjyRoomId("");
					player.setLmjyState(null);
					player.getPush().pushGameOver();
					HawkException.catchException(e);
				}
			}
			ActivityService.getInstance().dealMsg(GameConst.MsgId.ON_GUILD_BACK_DROP, new GuildBackDropInvoker(playerIds));
		} finally {
			try {
				GsApp.getInstance().removeObj(getParent().getXid());
			} catch (Exception e) {
				HawkException.catchException(e);
			}

			List<ILMJYPlayer> playerList = getParent().getPlayerList(PState.GAMEING, PState.PREJOIN);
			for (ILMJYPlayer player : playerList) {
				if (Objects.equal(player.getLmjyRoomId(), getParent().getId())) {
					player.setLmjyRoomId("");
					player.setLmjyState(null);
				}
			}
		}
		return true;
	}

	/** 结算 */
	private void jiesuan(ILMJYPlayer player) {
		PBLMJYGameOver.Builder builder = PBLMJYGameOver.newBuilder();
		try {
			LMJYBattleRoom battleRoom = getParent();
			builder.setGameUseTime(HawkTime.getMillisecond() - battleRoom.getStartTime());
			if (battleRoom.isCampAwin()) {
				builder.setAwards(battleRoom.getExtParm().getWinAward(player.getId()));
			}
			builder.setWin(battleRoom.isCampAwin());
			builder.setOverState(battleRoom.getOverState());
			List<ILMJYPlayer> playerList = battleRoom.getPlayerList(PState.GAMEING).stream()
					.filter(p -> p instanceof ILMJYPlayer)
					.sorted(Comparator.comparingInt(ILMJYPlayer::getKillCount).reversed())
					.collect(Collectors.toList());
			for (ILMJYPlayer gamer : playerList) {
				builder.addTeamPlayers(BuilderUtil.buildSnapshotData(gamer));
				builder.addKillInfo(PBLMJYKillInfo.newBuilder().setPlayerId(gamer.getId()).setKillCount(gamer.getKillCount()));
				if (!builder.hasVipsnapshot()) {
					builder.setVipsnapshot(BuilderUtil.buildSnapshotData(gamer));
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.LMJY_GAME_OVER, builder));
	}

}
