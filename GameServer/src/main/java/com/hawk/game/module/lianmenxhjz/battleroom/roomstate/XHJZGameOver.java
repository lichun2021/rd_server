package com.hawk.game.module.lianmenxhjz.battleroom.roomstate;

import java.util.List;

import com.hawk.game.service.xhjzWar.XHJZWarService;
import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.GsApp;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZBattleRoom;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZConst.XHJZState;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager.XHJZ_CAMP;
import com.hawk.game.module.lianmenxhjz.battleroom.msg.XHJZBilingInformationMsg;
import com.hawk.game.module.lianmenxhjz.battleroom.msg.XHJZQuitReason;
import com.hawk.game.module.lianmenxhjz.battleroom.player.IXHJZPlayer;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.IXHJZWorldMarch;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.XHJZ.PBXHJZGameOver;

public class XHJZGameOver extends IXHJZBattleRoomState {

	public XHJZGameOver(XHJZBattleRoom room) {
		super(room);

		// int playerCount = room.getPlayerList(XHJZState.GAMEING).size();
	}

	@Override
	public boolean onTick() {
		try {
			getParent().setGameOver(true);
			getParent().setLastSyncpb(null);
			getParent().buildSyncPB();
			XHJZRoomManager.getInstance().saveGailan(getParent().getLastSyncpb(), getParent().getId());
			// 行军删光
			for (IXHJZWorldMarch march : getParent().getWorldMarchList()) {
				march.remove();
			}
			XHJZBilingInformationMsg bilingInfo = new XHJZBilingInformationMsg();
			bilingInfo.setLastSyncpb(getParent().getLastSyncpb());
			bilingInfo.setRoomId(getParent().getId());
			
			// 战役结束消息
//			if (getParent().getExtParm().isLeaguaWar()) {
//				HawkApp.getInstance().postMsg(TiberiumLeagueWarService.getInstance(), bilingInfo);
//			} else {
//				HawkApp.getInstance().postMsg(TiberiumWarService.getInstance(), bilingInfo);
//			}
			HawkApp.getInstance().postMsg(XHJZWarService.getInstance(), bilingInfo);
			XHJZ_CAMP winCamp = XHJZ_CAMP.valueOf(getParent().getLastSyncpb().getWinCamp());
			for (IXHJZPlayer player : getParent().getPlayerList(XHJZState.GAMEING)) {
				try {
					getParent().sync(player);
					// 结算
					sendGameOver(winCamp, player);
				} catch (Exception e) {
					player.getData().unLockOriginalData();
					player.setXhjzRoomId("");
					player.setXhjzState(null);
					XHJZRoomManager.getInstance().invalidate(player);
					HawkException.catchException(e);
				}
			}
			// 发送主播
			for (IXHJZPlayer anchor : getParent().getAnchors()) {
				getParent().sync(anchor);
				sendGameOver(winCamp, anchor);
				XHJZRoomManager.getInstance().invalidate(anchor);
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			try {
				GsApp.getInstance().removeObj(getParent().getXid());
			} catch (Exception e) {
				HawkException.catchException(e);
			}

			List<IXHJZPlayer> playerList = getParent().getPlayerList(XHJZState.GAMEING);
			for (IXHJZPlayer player : playerList) {
				try {
					player.getData().unLockOriginalData();
					player.setXhjzRoomId("");
					player.setXhjzState(null);
					XHJZRoomManager.getInstance().invalidate(player);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
		return true;
	}

	private void sendGameOver(XHJZ_CAMP winCamp, IXHJZPlayer player) {
		PBXHJZGameOver.Builder builder = PBXHJZGameOver.newBuilder();
		builder.setPlayerHonor(player.getHonor());
		builder.setWin(player.getCamp() == winCamp);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_GAME_OVER, builder));
		player.setQuitReason(XHJZQuitReason.GAMEOVER);
		player.getPush().pushGameOver();
	}

}
