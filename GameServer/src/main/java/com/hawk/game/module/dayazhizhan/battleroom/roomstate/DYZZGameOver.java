package com.hawk.game.module.dayazhizhan.battleroom.roomstate;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.GsApp;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZConst.DYZZState;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;
import com.hawk.game.module.dayazhizhan.battleroom.msg.DYZZBilingInformationMsg;
import com.hawk.game.module.dayazhizhan.battleroom.msg.DYZZQuitReason;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.IDYZZWorldMarch;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZService;
import com.hawk.game.protocol.DYZZ.PBDYZZGameOver;
import com.hawk.game.protocol.HP;
import com.hawk.game.util.LogUtil;

public class DYZZGameOver extends IDYZZBattleRoomState {

	public DYZZGameOver(DYZZBattleRoom room) {
		super(room);
	}

	@Override
	public void init() {
		getParent().setGameOver(true);
		getParent().setOverTime(getParent().getCurTimeMil());
		Optional<IDYZZPlayer> mvpA = getParent().getCampPlayers(DYZZCAMP.A).stream().sorted(Comparator.comparingInt(IDYZZPlayer::getKda).reversed()).findFirst();
		if (mvpA.isPresent() && mvpA.get().getNegative() <= 0) {
			mvpA.get().setMvp(1);
		}
		Optional<IDYZZPlayer> mvpB = getParent().getCampPlayers(DYZZCAMP.B).stream().sorted(Comparator.comparingInt(IDYZZPlayer::getKda).reversed()).findFirst();
		if (mvpB.isPresent() && mvpB.get().getNegative() <= 0) {
			mvpB.get().setMvp(1);
		}
		//计算赛季积分
		getParent().getPlayerList(DYZZState.GAMEING).stream().forEach(IDYZZPlayer::calSeasonScoreAdd);
		//计算首胜奖励
		getParent().getPlayerList(DYZZState.GAMEING).stream().forEach(IDYZZPlayer::calSeasonFirstWin);
		
		getParent().setLastSyncpb(null);
		getParent().buildSyncPB();
		// 行军删光
		for (IDYZZWorldMarch march : getParent().getWorldMarchList()) {
			march.remove();
		}
		for (IDYZZPlayer player : getParent().getPlayerList(DYZZState.GAMEING)) {
			getParent().sync(player);
			player.cleanTempData();
		}

	}

	@Override
	public boolean onTick() {
		if (getParent().getCurTimeMil() - getParent().getOverTime() < 1000) {
			return true;
		}

		DYZZCAMP winCamp = getParent().getWinCamp();
		for (IDYZZPlayer player : getParent().getPlayerList(DYZZState.GAMEING)) {
			try {
				sendGameOver(winCamp, player);
				player.getPush().pushGameOver();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		try {

			DYZZBilingInformationMsg bilingInfo = new DYZZBilingInformationMsg();
			bilingInfo.setLastSyncpb(getParent().getLastSyncpb());
			bilingInfo.setRoomId(getParent().getId());

			// 战役结束消息
			HawkApp.getInstance().postMsg(DYZZService.getInstance(), bilingInfo);

			// LOG--------------------------------------------------
			LogUtil.logDYZZResult(getParent().getId(), getParent().getCampGuild(winCamp), bilingInfo.getBaseHPA(), bilingInfo.getBaseHPB());
			// LOG--------------------------------------------------
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		try {
			GsApp.getInstance().removeObj(getParent().getXid());
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		List<IDYZZPlayer> playerList = getParent().getPlayerList(DYZZState.GAMEING);
		for (IDYZZPlayer player : playerList) {
			try {
				player.getData().unLockOriginalData();
				player.setDYZZRoomId("");
				player.setDYZZState(null);
				DYZZRoomManager.getInstance().invalidate(player);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return true;
	}

	private void sendGameOver(DYZZCAMP winCamp, IDYZZPlayer player) {
		player.setQuitReason(DYZZQuitReason.GAMEOVER);
		PBDYZZGameOver.Builder builder = PBDYZZGameOver.newBuilder();
		builder.setWin(player.getCamp() == winCamp);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_GAME_OVER, builder));
	}

}
