package com.hawk.game.lianmengstarwars.roomstate;

import java.util.List;

import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.GsApp;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.lianmengstarwars.SWBattleRoom;
import com.hawk.game.lianmengstarwars.SWConst.SWState;
import com.hawk.game.lianmengstarwars.SWRoomManager;
import com.hawk.game.lianmengstarwars.msg.SWBilingInformationMsg;
import com.hawk.game.lianmengstarwars.msg.SWQuitReason;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.lianmengstarwars.worldmarch.ISWWorldMarch;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SW.PBSWGameOver;
import com.hawk.game.protocol.SW.PBSWVideoPackage.Builder;
import com.hawk.game.service.starwars.SWPlayerData;
import com.hawk.game.service.starwars.StarWarsActivityService;
import com.hawk.game.service.starwars.StarWarsConst;

public class SWGameOver extends ISWBattleRoomState {
	private boolean killGame;
	public SWGameOver(SWBattleRoom room) {
		super(room);
	}

	@Override
	public boolean onTick() {
		try {
			// 行军删光
			boolean marchClean = true;
			for (ISWWorldMarch march : getParent().getWorldMarchList()) {
				march.onMarchBack();
				march.remove();
				marchClean = false;
			}
			if (!marchClean) {
				return true;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		calBattleResult();
		return true;
	}

	private void calBattleResult() {
		if (getParent().isGameOver()) {
			return;
		}
		try {

//			if (!killGame) {
				// StarWarsActivityService 736 sendResultMail 发放奖励，顺便发放 排名， 和击杀奖励。 以联盟为单位
				SWBilingInformationMsg bilingInfo = new SWBilingInformationMsg();
				bilingInfo.setLastSyncpb(getParent().getLastBilingpb());
				bilingInfo.setRoomId(getParent().getId());
				bilingInfo.setWinGuild(getParent().getWinGuild());
				bilingInfo.setWarType(getParent().getExtParm().getWarType());
				bilingInfo.setOverType(getParent().getOverType());
				// 战役结束消息
				HawkApp.getInstance().postMsg(StarWarsActivityService.getInstance(), bilingInfo);
//			}

			for (ISWPlayer player : getParent().getPlayerList(SWState.GAMEING)) {
				try {
					// 结算
					PBSWGameOver.Builder builder = PBSWGameOver.newBuilder();
					builder.setPlayerHonor(player.getHonor());
					builder.setWin(player.getGuildId().equals(getParent().getWinGuild()));
					player.sendProtocol(HawkProtocol.valueOf(HP.code.SW_GAME_OVER, builder));
					player.setSWQuitReason(SWQuitReason.GAMEOVER);
					player.getPush().pushGameOver();
					try {
						int termId = StarWarsActivityService.getInstance().getTermId();
						StarWarsConst.SWWarType warType = StarWarsActivityService.getInstance().getCurrWarType();
						SWPlayerData playerDate = RedisProxy.getInstance().getSWPlayerData(player.getId(), termId, warType.getNumber());
						if(playerDate != null){
							playerDate.setKillPower((long) player.getKillPower());
							playerDate.setDeadPower(player.getDeadPower());
							RedisProxy.getInstance().updateSWPlayerData(playerDate, termId, warType.getNumber());
						}
					}catch (Exception e){
						HawkException.catchException(e);
					}
				} catch (Exception e) {
					player.getData().unLockOriginalData();
					player.setSwRoomId("");
					player.setSwState(null);
					SWRoomManager.getInstance().invalidate(player);
					HawkException.catchException(e);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			getParent().setGameOver(true);
			try {
				GsApp.getInstance().removeObj(getParent().getXid());
			} catch (Exception e) {
				HawkException.catchException(e);
			}

			List<ISWPlayer> playerList = getParent().getPlayerList(SWState.GAMEING);
			for (ISWPlayer player : playerList) {
				try {
					player.getData().unLockOriginalData();
					player.setSwRoomId("");
					player.setSwState(null);
					SWRoomManager.getInstance().invalidate(player);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
	}

	public boolean isKillGame() {
		return killGame;
	}

	public void setKillGame(boolean killGame) {
		this.killGame = killGame;
	}

}
