package com.hawk.game.lianmengcyb.roomstate;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.GsApp;
import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.lianmengcyb.CYBORGConst.CYBORGState;
import com.hawk.game.lianmengcyb.CYBORGRoomManager;
import com.hawk.game.lianmengcyb.CYBORGRoomManager.CYBORG_CAMP;
import com.hawk.game.lianmengcyb.msg.CYBORGBilingInformationMsg;
import com.hawk.game.lianmengcyb.msg.CYBORGQuitReason;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldmarch.ICYBORGWorldMarch;
import com.hawk.game.lianmengcyb.worldpoint.ICYBORGBuilding;
import com.hawk.game.protocol.CYBORG.PBCYBORGGameOver;
import com.hawk.game.protocol.CYBORG.PBCYBORGGuildInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.service.cyborgWar.CyborgWarService;

public class CYBORGGameOver extends ICYBORGBattleRoomState {
	private long overtime;
	public CYBORGGameOver(CYBORGBattleRoom room) {
		super(room);
		overtime = room.getCurTimeMil();
		// int playerCount = room.getPlayerList(CYBORGState.GAMEING).size();
	}

	@Override
	public boolean onTick() {
		if (getParent().getCurTimeMil() - overtime < 1500) { // 先等一下 防止有事件抛出待处理
			return true;
		}
		try {
			getParent().setGameOver(true);
			// 行军删光
			for (ICYBORGWorldMarch march : getParent().getWorldMarchList()) {
				march.remove();
			}
			CYBORGBilingInformationMsg bilingInfo = new CYBORGBilingInformationMsg();
			bilingInfo.init(getParent().getLastSyncpb(),
					getParent().getCampBase(CYBORG_CAMP.A).campGuild,
					getParent().getCampBase(CYBORG_CAMP.B).campGuild,
					getParent().getCampBase(CYBORG_CAMP.C).campGuild,
					getParent().getCampBase(CYBORG_CAMP.D).campGuild);
			bilingInfo.setRoomId(getParent().getId());
			bilingInfo.setLeaguaWar(getParent().getExtParm().isLeaguaWar());
			bilingInfo.setSeason(getParent().getExtParm().getSeason());
			for (ICYBORGBuilding build : getParent().getCYBORGBuildingList()) {
				bilingInfo.addBuildRecord(build); // 结算
			}

			HawkApp.getInstance().postMsg(CyborgWarService.getInstance(), bilingInfo);

			List<Integer> winGuild = getParent().getLastSyncpb().getGuildInfoList().stream()
					.sorted(Comparator.comparingInt(PBCYBORGGuildInfo::getHonor).reversed())
					.map(PBCYBORGGuildInfo:: getCamp)
					.collect(Collectors.toList());
			CYBORG_CAMP winCamp = CYBORG_CAMP.valueOf(winGuild.get(0));
			for (ICYBORGPlayer player : getParent().getPlayerList(CYBORGState.GAMEING)) {
				try {
					// 结算
					sendGameOver(winCamp, player, winGuild.indexOf(player.getCamp().intValue()) + 1);
				} catch (Exception e) {
					player.getData().unLockOriginalData();
					player.setCYBORGRoomId("");
					player.setCYBORGState(null);
					CYBORGRoomManager.getInstance().invalidate(player);
					HawkException.catchException(e);
				}
			}
			// 发送主播
			if (Objects.nonNull(getParent().getAnchor())) {
				ICYBORGPlayer anchor = getParent().getAnchor();
				sendGameOver(winCamp, anchor, winGuild.indexOf(anchor.getCamp().intValue()) + 1);
				CYBORGRoomManager.getInstance().invalidate(anchor);
			}

			// LOG--------------------------------------------------
			// String winGuild = winCamp==CAMP.A?getParent().getCampAGuild():getParent().getCampBGuild();
			// LogUtil.logCYBORGResult(bilingInfo, winGuild, campAScore, campBScore, getParent().campANuclearSendCount, getParent().campBNuclearSendCount);
			// for(ICYBORGBuilding building: getParent().getCYBORGBuildingList()){
			// int abuildScore = building.getControlGuildHonorMap().getOrDefault(getParent().getCampAGuild(), 0D).intValue();
			// int bbuildScore = building.getControlGuildHonorMap().getOrDefault(getParent().getCampBGuild(), 0D).intValue();
			// int campAControl = (int) building.getControlGuildTimeMap().get(getParent().getCampAGuild())/1000;
			// int campBControl = (int) building.getControlGuildTimeMap().get(getParent().getCampAGuild())/1000;
			// LogUtil.logCYBORGBuilding(bilingInfo, abuildScore, bbuildScore, campAControl, campBControl);
			// }
			// LOG--------------------------------------------------
		} finally {
			try {
				GsApp.getInstance().removeObj(getParent().getXid());
			} catch (Exception e) {
				HawkException.catchException(e);
			}

			List<ICYBORGPlayer> playerList = getParent().getPlayerList(CYBORGState.GAMEING);
			for (ICYBORGPlayer player : playerList) {
				try {
					player.getData().unLockOriginalData();
					player.setCYBORGRoomId("");
					player.setCYBORGState(null);
					CYBORGRoomManager.getInstance().invalidate(player);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
		return true;
	}

	private void sendGameOver(CYBORG_CAMP winCamp, ICYBORGPlayer player , int index) {
		PBCYBORGGameOver.Builder builder = PBCYBORGGameOver.newBuilder();
		builder.setPlayerHonor(player.getHonor());
		builder.setWin(player.getCamp() == winCamp);
		builder.setMingci(index);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_GAME_OVER, builder));
		player.setQuitReason(CYBORGQuitReason.GAMEOVER);
		player.getPush().pushGameOver();
	}

}
