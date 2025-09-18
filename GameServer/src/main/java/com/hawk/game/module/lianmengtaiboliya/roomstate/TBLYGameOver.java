package com.hawk.game.module.lianmengtaiboliya.roomstate;

import java.util.List;

import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.GsApp;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager.CAMP;
import com.hawk.game.module.lianmengtaiboliya.msg.QuitReason;
import com.hawk.game.module.lianmengtaiboliya.msg.TBLYBilingInformationMsg;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.ITBLYWorldMarch;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.ITBLYBuilding;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.TBLY.PBGuildInfo;
import com.hawk.game.protocol.TBLY.PBTBLYGameOver;
import com.hawk.game.service.tiberium.TiberiumLeagueWarService;
import com.hawk.game.service.tiberium.TiberiumWarService;
import com.hawk.game.util.LogUtil;

public class TBLYGameOver extends ITBLYBattleRoomState {
	private long overtime;
	public TBLYGameOver(TBLYBattleRoom room) {
		super(room);

		overtime = room.getCurTimeMil();
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
			TBLYRoomManager.getInstance().saveGailan(getParent().getLastSyncpb(), getParent().getId());
			// 行军删光
			for (ITBLYWorldMarch march : getParent().getWorldMarchList()) {
				march.remove();
			}
			TBLYBilingInformationMsg bilingInfo = new TBLYBilingInformationMsg();
			bilingInfo.init(getParent().getLastSyncpb(), getParent().getCampAGuild(), getParent().getCampBGuild());
			bilingInfo.setRoomId(getParent().getId());
			bilingInfo.setLeaguaWar(getParent().getExtParm().isLeaguaWar());
			bilingInfo.setSeason(getParent().getExtParm().getSeason());
			bilingInfo.setFirst5000Honor(getParent().first5000Honor);
			bilingInfo.setFirstControlHeXin(getParent().firstControlHeXin);
			bilingInfo.setFirstKillNian(getParent().firstKillNian);
			for (ITBLYBuilding build : getParent().getTBLYBuildingList()) {
				bilingInfo.addBuildRecord(build); // 结算
			}
			
			// 战役结束消息
			if (getParent().getExtParm().isLeaguaWar()) {
				HawkApp.getInstance().postMsg(TBLYSeasonService.getInstance(), bilingInfo);
			} else {
				HawkApp.getInstance().postMsg(TBLYWarService.getInstance(), bilingInfo);
			}
			PBGuildInfo campAguildInfo = bilingInfo.getGuildInfo(getParent().getCampAGuild());
			PBGuildInfo campBguildInfo = bilingInfo.getGuildInfo(getParent().getCampBGuild());
			long campAScore = campAguildInfo.getHonor();
			long campBScore = campBguildInfo.getHonor();
			CAMP winCamp = campAScore > campBScore ? CAMP.A : CAMP.B;
			for (ITBLYPlayer player : getParent().getPlayerList(TBLYState.GAMEING)) {
				try {
					getParent().sync(player);
					// 结算
					sendGameOver(winCamp, player);
				} catch (Exception e) {
					player.getData().unLockOriginalData();
					player.setTBLYRoomId("");
					player.setTBLYState(null);
					TBLYRoomManager.getInstance().invalidate(player);
					HawkException.catchException(e);
				}
			}
			// 发送主播
			for (ITBLYPlayer anchor : getParent().getAnchors()) {
				getParent().sync(anchor);
				sendGameOver(winCamp, anchor);
				TBLYRoomManager.getInstance().invalidate(anchor);
			}

			//LOG--------------------------------------------------
			String winGuild = winCamp==CAMP.A?getParent().getCampAGuild():getParent().getCampBGuild();
			bilingInfo.setWinGuild(winGuild);
			DungeonRedisLog.log(getParent().getId(), "winGuild:{} camp: {} guildA:{} scoreA:{} guildB:{} socreB:{}", winGuild, winCamp, bilingInfo.getCampAGuild(), campAScore,
					bilingInfo.getCampBGuild(), campBScore);
			LogUtil.logTBLYResult(bilingInfo, winGuild, campAScore, campBScore, campAguildInfo.getBuildControlHonor(), campBguildInfo.getBuildControlHonor(),
					campAguildInfo.getCollectHonor(), campBguildInfo.getCollectHonor(), getParent().campANuclearSendCount, getParent().campBNuclearSendCount,
					getParent().extryHonorA, getParent().extryHonorB, getParent().firstControlHonorA,getParent().firstControlHonorB);
			for(ITBLYBuilding building: getParent().getTBLYBuildingList()){
				int abuildScore = building.getControlGuildHonorMap().getOrDefault(getParent().getCampAGuild(),  0D).intValue();
				int bbuildScore = building.getControlGuildHonorMap().getOrDefault(getParent().getCampBGuild(),  0D).intValue();
				int campAControl = (int) building.getControlGuildTimeMap().get(getParent().getCampAGuild())/1000;
				int campBControl = (int) building.getControlGuildTimeMap().get(getParent().getCampBGuild())/1000;
				LogUtil.logTBLYBuilding(bilingInfo, building.getPointId(), abuildScore, bbuildScore, campAControl, campBControl);
			}
			//LOG--------------------------------------------------
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			try {
				GsApp.getInstance().removeObj(getParent().getXid());
			} catch (Exception e) {
				HawkException.catchException(e);
			}

			List<ITBLYPlayer> playerList = getParent().getPlayerList(TBLYState.GAMEING);
			for (ITBLYPlayer player : playerList) {
				try {
					player.getData().unLockOriginalData();
					player.setTBLYRoomId("");
					player.setTBLYState(null);
					TBLYRoomManager.getInstance().invalidate(player);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
		return true;
	}

	private void sendGameOver(CAMP winCamp, ITBLYPlayer player) {
		PBTBLYGameOver.Builder builder = PBTBLYGameOver.newBuilder();
		builder.setPlayerHonor(player.getHonor());
		builder.setWin(player.getCamp() == winCamp);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_GAME_OVER, builder));
		player.setQuitReason(QuitReason.GAMEOVER);
		player.getPush().pushGameOver();
	}

}
