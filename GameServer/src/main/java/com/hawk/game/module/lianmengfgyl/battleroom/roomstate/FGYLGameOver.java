package com.hawk.game.module.lianmengfgyl.battleroom.roomstate;

import java.util.List;

import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.GsApp;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLBattleRoom;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLConst.FGYLState;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager.FGYL_CAMP;
import com.hawk.game.module.lianmengfgyl.battleroom.msg.FGYLBilingInformationMsg;
import com.hawk.game.module.lianmengfgyl.battleroom.msg.FGYLQuitReason;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.IFGYLWorldMarch;
import com.hawk.game.module.lianmengfgyl.march.service.FGYLMatchService;
import com.hawk.game.protocol.FGYL.PBFGYLGameOver;
import com.hawk.game.protocol.HP;

public class FGYLGameOver extends IFGYLBattleRoomState {

	public FGYLGameOver(FGYLBattleRoom room) {
		super(room);

		// int playerCount = room.getPlayerList(FGYLState.GAMEING).size();
	}

	@Override
	public boolean onTick() {
		try {
			getParent().setGameOver(true);
			getParent().setLastSyncpb(null);
			getParent().buildSyncPB();
			FGYLRoomManager.getInstance().saveGailan(getParent().getLastSyncpb(), getParent().getId());
			// 行军删光
			for (IFGYLWorldMarch march : getParent().getWorldMarchList()) {
				march.remove();
			}
			FGYLBilingInformationMsg bilingInfo = new FGYLBilingInformationMsg();
			bilingInfo.setLastSyncpb(getParent().getLastSyncpb());
			bilingInfo.setRoomId(getParent().getId());
			
			// 战役结束消息
//			if (getParent().getExtParm().isLeaguaWar()) {
//				HawkApp.getInstance().postMsg(TiberiumLeagueWarService.getInstance(), bilingInfo);
//			} else {
//				HawkApp.getInstance().postMsg(TiberiumWarService.getInstance(), bilingInfo);
//			}
			HawkApp.getInstance().postMsg(FGYLMatchService.getInstance(), bilingInfo);
			FGYL_CAMP winCamp = FGYL_CAMP.valueOf(getParent().getLastSyncpb().getWinCamp());
			for (IFGYLPlayer player : getParent().getPlayerList(FGYLState.GAMEING)) {
				try {
					getParent().sync(player);
					// 结算
					sendGameOver(winCamp, player);
				} catch (Exception e) {
					player.getData().unLockOriginalData();
					player.setFgylRoomId("");
					player.setFgylState(null);
					FGYLRoomManager.getInstance().invalidate(player);
					HawkException.catchException(e);
				}
			}
			// 发送主播
			for (IFGYLPlayer anchor : getParent().getAnchors()) {
				getParent().sync(anchor);
				sendGameOver(winCamp, anchor);
				FGYLRoomManager.getInstance().invalidate(anchor);
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			try {
				GsApp.getInstance().removeObj(getParent().getXid());
			} catch (Exception e) {
				HawkException.catchException(e);
			}

			List<IFGYLPlayer> playerList = getParent().getPlayerList(FGYLState.GAMEING);
			for (IFGYLPlayer player : playerList) {
				try {
					player.getData().unLockOriginalData();
					player.setFgylRoomId("");
					player.setFgylState(null);
					FGYLRoomManager.getInstance().invalidate(player);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
		return true;
	}

	private void sendGameOver(FGYL_CAMP winCamp, IFGYLPlayer player) {
		PBFGYLGameOver.Builder builder = PBFGYLGameOver.newBuilder();
		builder.setPlayerHonor(player.getHonor());
		builder.setWin(player.getCamp() == winCamp);
		builder.setGametime(getParent().getGametime());
		builder.addAllFgylHurtRank(getParent().foggyHurtRank());
		builder.addAllFgylMonsterRank(getParent().monsterKillRank());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_GAME_OVER, builder));
		player.setQuitReason(FGYLQuitReason.GAMEOVER);
		player.getPush().pushGameOver();
	}

}
