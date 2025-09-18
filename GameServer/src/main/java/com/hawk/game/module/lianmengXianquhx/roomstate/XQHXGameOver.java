package com.hawk.game.module.lianmengXianquhx.roomstate;

import java.util.List;

import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.GsApp;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengXianquhx.XQHXBattleRoom;
import com.hawk.game.module.lianmengXianquhx.XQHXConst.XQHXState;
import com.hawk.game.module.lianmengXianquhx.XQHXRoomManager;
import com.hawk.game.module.lianmengXianquhx.XQHXRoomManager.XQHX_CAMP;
import com.hawk.game.module.lianmengXianquhx.msg.XQHXBilingInformationMsg;
import com.hawk.game.module.lianmengXianquhx.msg.XQHXQuitReason;
import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;
import com.hawk.game.module.lianmengXianquhx.worldmarch.IXQHXWorldMarch;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.XQHX.PBGuildInfo;
import com.hawk.game.protocol.XQHX.PBXQHXGameOver;
import com.hawk.game.service.xqhxWar.XQHXWarService;

public class XQHXGameOver extends IXQHXBattleRoomState {
	private long overtime;
	public XQHXGameOver(XQHXBattleRoom room) {
		super(room);
		overtime = room.getCurTimeMil();
		// int playerCount = room.getPlayerList(XQHXState.GAMEING).size();
	}

	@Override
	public boolean onTick() {
		if (getParent().getCurTimeMil() - overtime < 1500) { // 先等一下 防止有事件抛出待处理
			return true;
		}
		try {
			getParent().setGameOver(true);
			getParent().setLastSyncpb(null);
			getParent().buildSyncPB();
			XQHXRoomManager.getInstance().saveGailan(getParent().getLastSyncpb(), getParent().getId());
			// 行军删光
			for (IXQHXWorldMarch march : getParent().getWorldMarchList()) {
				march.remove();
			}
			XQHXBilingInformationMsg bilingInfo = new XQHXBilingInformationMsg();
			bilingInfo.setLastSyncpb(getParent().getLastSyncpb());
			bilingInfo.setRoomId(getParent().getId());
			PBGuildInfo campAguildInfo = bilingInfo.getGuildInfo(getParent().getCampGuild(XQHX_CAMP.A));
			PBGuildInfo campBguildInfo = bilingInfo.getGuildInfo(getParent().getCampGuild(XQHX_CAMP.B));
			long campAScore = campAguildInfo.getHonor();
			long campBScore = campBguildInfo.getHonor();
			XQHX_CAMP winCamp = XQHX_CAMP.valueOf(getParent().getLastSyncpb().getWinCamp());
			for (IXQHXPlayer player : getParent().getPlayerList(XQHXState.GAMEING)) {
				try {
					getParent().sync(player);
					// 结算
					sendGameOver(winCamp, player);
				} catch (Exception e) {
					player.getData().unLockOriginalData();
					player.setXQHXRoomId("");
					player.setXQHXState(null);
					XQHXRoomManager.getInstance().invalidate(player);
					HawkException.catchException(e);
				}
			}
			// 发送主播
			for (IXQHXPlayer anchor : getParent().getAnchors()) {
				getParent().sync(anchor);
				sendGameOver(winCamp, anchor);
				XQHXRoomManager.getInstance().invalidate(anchor);
			}

			//LOG--------------------------------------------------
			String winGuild = getParent().getCampGuild(winCamp);
			bilingInfo.setWinGuild(winGuild);
			
			HawkApp.getInstance().postMsg(XQHXWarService.getInstance(), bilingInfo);
			
			DungeonRedisLog.log(getParent().getId(), "winGuild:{} camp: {} guildA:{} scoreA:{} guildB:{} socreB:{}", winGuild, winCamp, getParent().getCampGuild(XQHX_CAMP.A), campAScore,
					getParent().getCampGuild(XQHX_CAMP.B), campBScore);
//			LogUtil.logXQHXResult(bilingInfo, winGuild, campAScore, campBScore, campAguildInfo.getBuildControlHonor(), campBguildInfo.getBuildControlHonor(),
//					campAguildInfo.getCollectHonor(), campBguildInfo.getCollectHonor(), getParent().campANuclearSendCount, getParent().campBNuclearSendCount,
//					getParent().extryHonorA, getParent().extryHonorB, getParent().firstControlHonorA,getParent().firstControlHonorB);
//			for(IXQHXBuilding building: getParent().getXQHXBuildingList()){
//				int abuildScore = building.getControlGuildHonorMap().getOrDefault(getParent().getCampAGuild(),  0D).intValue();
//				int bbuildScore = building.getControlGuildHonorMap().getOrDefault(getParent().getCampBGuild(),  0D).intValue();
//				int campAControl = (int) building.getControlGuildTimeMap().get(getParent().getCampAGuild())/1000;
//				int campBControl = (int) building.getControlGuildTimeMap().get(getParent().getCampBGuild())/1000;
//				LogUtil.logXQHXBuilding(bilingInfo, building.getPointId(), abuildScore, bbuildScore, campAControl, campBControl);
//			}
			//LOG--------------------------------------------------
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			try {
				GsApp.getInstance().removeObj(getParent().getXid());
			} catch (Exception e) {
				HawkException.catchException(e);
			}

			List<IXQHXPlayer> playerList = getParent().getPlayerList(XQHXState.GAMEING);
			for (IXQHXPlayer player : playerList) {
				try {
					player.getData().unLockOriginalData();
					player.setXQHXRoomId("");
					player.setXQHXState(null);
					XQHXRoomManager.getInstance().invalidate(player);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
		return true;
	}

	private void sendGameOver(XQHX_CAMP winCamp, IXQHXPlayer player) {
		PBXQHXGameOver.Builder builder = PBXQHXGameOver.newBuilder();
		builder.setPlayerHonor(player.getHonor());
		builder.setWin(player.getCamp() == winCamp);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_GAME_OVER, builder));
		player.setQuitReason(XQHXQuitReason.GAMEOVER);
		player.getPush().pushGameOver();
	}

}
