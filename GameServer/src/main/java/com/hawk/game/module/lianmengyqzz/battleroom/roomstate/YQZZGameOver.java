package com.hawk.game.module.lianmengyqzz.battleroom.roomstate;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.GsApp;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst.YQZZState;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZRoomManager;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZ_CAMP;
import com.hawk.game.module.lianmengyqzz.battleroom.msg.YQZZBilingInformationMsg;
import com.hawk.game.module.lianmengyqzz.battleroom.msg.YQZZQuitReason;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.YQZZ.PBYQZZGameOver;
import com.hawk.game.protocol.YQZZ.PBYQZZGuildInfo;

public class YQZZGameOver extends IYQZZBattleRoomState {
	private long overtime;

	public YQZZGameOver(YQZZBattleRoom room) {
		super(room);
		getParent().setGameOver(true);
		overtime = room.getCurTimeMil();
	}

	@Override
	public boolean onTick() {
		if (getParent().getCurTimeMil() - overtime < 1500) { // 先等一下 防止有事件抛出待处理
			return true;
		}

		try {
//			getParent().setLastSyncpb(null);
//			getParent().buildSyncPB();

			// 行军删光
			for (IYQZZWorldMarch march : getParent().getWorldMarchList()) {
				march.remove();
			}

			Integer winGuild = getParent().getLastSyncpb().getGuildInfoList().stream()
					.sorted(Comparator.comparingInt(PBYQZZGuildInfo::getHonor).reversed())
					.map(PBYQZZGuildInfo::getCamp)
					.findFirst().get();
			YQZZ_CAMP winCamp = YQZZ_CAMP.valueOf(winGuild);
			YQZZBattleData statisticsData = new YQZZBattleData();
			statisticsData.parserData(getParent().getLastSyncpb(),
					getParent().getExtParm().getBattleId());
			for (IYQZZPlayer player : getParent().getPlayerList(YQZZState.GAMEING)) {
				try {
//					getParent().sync(player);
					// 结算
					sendGameOver(winCamp, player, statisticsData);
				} catch (Exception e) {
					player.getData().unLockOriginalData();
					player.setYQZZRoomId("");
					player.setYQZZState(null);
					YQZZRoomManager.getInstance().invalidate(player);
					HawkException.catchException(e);
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			try {
				GsApp.getInstance().removeObj(getParent().getXid());

				List<IYQZZPlayer> playerList = getParent().getPlayerList(YQZZState.GAMEING);
				for (IYQZZPlayer player : playerList) {
					try {
						player.getData().unLockOriginalData();
						player.setYQZZRoomId("");
						player.setYQZZState(null);
						YQZZRoomManager.getInstance().invalidate(player);
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}

			YQZZBilingInformationMsg bilingInfo = new YQZZBilingInformationMsg();
			bilingInfo.setRoomId(getParent().getId());
			bilingInfo.setLastSyncpb(getParent().getLastSyncpb());
			HawkApp.getInstance().postMsg(YQZZMatchService.getInstance(), bilingInfo);

			getParent().gameOver();
		}
		return true;
	}

	private void sendGameOver(YQZZ_CAMP winCamp, IYQZZPlayer player, YQZZBattleData statisticsData) {
		int contryRank = statisticsData.getCountryRank(player.getMainServerId());
		int contryCount = statisticsData.getCountryCount();
		int guildRank = statisticsData.getGuildRank(player.getGuildId());
		int guildCount = statisticsData.getGuildCount();
		int playerRank = statisticsData.getPlayerRank(player.getId());

		PBYQZZGameOver.Builder builder = PBYQZZGameOver.newBuilder();
		builder.setPlayerHonor(player.getHonor());
		builder.setWin(player.getCamp() == winCamp);
		builder.setCountryRank(contryRank);
		builder.setCountryCount(contryCount);
		builder.setGuildRank(guildRank);
		builder.setGuildCount(guildCount);
		builder.setPlayerRank(playerRank);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_GAME_OVER, builder));
		player.setQuitReason(YQZZQuitReason.GAMEOVER);
		player.getPush().pushGameOver();
	}

}
