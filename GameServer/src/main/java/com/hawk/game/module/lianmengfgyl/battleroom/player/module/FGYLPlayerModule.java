package com.hawk.game.module.lianmengfgyl.battleroom.player.module;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.hawk.annotation.MessageHandler;

import com.google.common.base.Joiner;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLConst.FGYLState;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager;
import com.hawk.game.module.lianmengfgyl.battleroom.msg.FGYLJoinRoomMsg;
import com.hawk.game.module.lianmengfgyl.battleroom.msg.FGYLQuitRoomMsg;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.player.FGYLPlayer;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;

public class FGYLPlayerModule extends PlayerModule {

	public FGYLPlayerModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		IFGYLPlayer gamer = FGYLRoomManager.getInstance().makesurePlayer(player.getId());
		if (gamer != null && gamer.getParent() != null) {
//			if(gamer.isAnchor()){
//				gamer.getParent().anchorJoinRoom(gamer);
//				return true;
//			}
			
			boolean needClean = gamer.getParent().getPlayer(player.getId()) == null || gamer.getParent().isGameOver();
			if (needClean) {
				gamer.getData().unLockOriginalData();
				gamer.setFgylRoomId("");
				gamer.setFgylState(null);
				FGYLRoomManager.getInstance().invalidate(gamer);
				return true;
			}
		}

		if (Objects.nonNull(gamer)) {
			FGYLPlayer tblyplayer = (FGYLPlayer) gamer;
			player.setFgylState(FGYLState.GAMEING);
			player.setFgylRoomId(tblyplayer.getParent().getId());
			tblyplayer.setSource(player);

			gamer.getParent().onPlayerLogin(gamer);
		} else {
			player.setFgylRoomId("");
			player.setFgylState(null);
			return true;
		}
		return super.onPlayerLogin();
	}

	@MessageHandler
	private void onJoinRoomMsg(FGYLJoinRoomMsg msg) {
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
		// 记录玩家兵力
//		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.FGYL_JOIN_ROOM,
//				Params.valueOf("Army", armyStr));
		DungeonRedisLog.log(player.getId(), "roomId {} , army {}", player.getDYZZRoomId(), armyStr);
		msg.getBattleRoom().joinRoom(msg.getPlayer());
	}

	/** 结束, 记录胜负什么的 */
	@MessageHandler
	private void onQuitRoomMsg(FGYLQuitRoomMsg msg) {
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
		// 记录玩家兵力
//		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.FGYL_QUIT_ROOM,
//				Params.valueOf("Army", armyStr));
		DungeonRedisLog.log(player.getId(), "reason {} army {}", msg.getQuitReason(), armyStr);
		Map<String, String> csmap = RedisProxy.getInstance().jbsTakeOutAllCreateSoldier(player.getId());
		if (csmap.isEmpty()) {
			return;
		}
		String items = "";
		for (Entry<String, String> ent : csmap.entrySet()) {
			items += String.format("70000_%s_%s,", ent.getKey(), ent.getValue());
		}

		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.FGYL_BACK_SOLDIER)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				// .addSubTitles("叫爸爸!!!!")
				// .addContents("哥把兵让你带出副本, 叫爸爸!!!")
				.setRewards(items)
				.build());
	}

//	/** 主翻进房间 */
//	@ProtocolHandler(code = HP.code.FGYL_ANCHOR_JOIN_REQ_VALUE)
//	private void onAnchorJoin(HawkProtocol protocol) {
//		if (!GameUtil.isOBPuidCtrlPlayer(player.getOpenId())) {
//			return;
//		}
//		
//		PBFGYLAnchorJoinReq req = protocol.parseProtocol(PBFGYLAnchorJoinReq.getDefaultInstance());
//		final String battleId = req.getBattleId();
//		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.FGYLAOGUAN_ROOM, battleId);
//		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.FGYLAOGUAN_ROOM).queryObject(roomXid);
//		FGYLBattleRoom room = null;
//		if (roomObj != null) {
//			room = (FGYLBattleRoom) roomObj.getImpl();
//		} else {
//			sendError(protocol.getType(), Status.Error.TIBERIUM_OB_NO_GAME);
//			return;
//		}
//		player.setFgylRoomId(roomXid.getUUID());
//
//		IFGYLPlayer hp = new FGYLPlayer(player);
//		hp.setParent(room);
//		room.anchorJoinRoom(hp);
//	}
//	
//	@ProtocolHandler(code = HP.code2.FGYL_LASTSNC_REQ_VALUE)
//	private void onGetSyncPb(HawkProtocol protocol) {
//		String guildId = player.getGuildId();
//		if (HawkOSOperator.isEmptyString(guildId)) {
//			return;
//		}
//
//		boolean isInLeaguaWar = TiberiumLeagueWarService.getInstance().isJointLeaguaWar(guildId);
//		int termId = 0;
//		if (isInLeaguaWar) {
//			termId = TiberiumLeagueWarService.getInstance().getActivityInfo().getMark();
//		} else {
//			termId = TiberiumWarService.getInstance().getActivityData().getTermId();
//		}
//		TWGuildData guildData = RedisProxy.getInstance().getTWGuildData(guildId, termId);
//		if (guildData == null) {
//			return;
//		}
//		String roomId = guildData.getRoomId();
//		if (HawkOSOperator.isEmptyString(guildData.getRoomId())) {
//			return;
//		}
//		PBFGYLGameInfoSync resp = FGYLRoomManager.getInstance().getGaiLanResp(roomId);
//		
//		player.sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_LASTSNC_RESP, resp.toBuilder()));
//	}

}
