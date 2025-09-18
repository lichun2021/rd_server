package com.hawk.game.module.lianmenxhjz.battleroom.player.module;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.hawk.annotation.MessageHandler;

import com.google.common.base.Joiner;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZConst.XHJZState;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager;
import com.hawk.game.module.lianmenxhjz.battleroom.msg.XHJZJoinRoomMsg;
import com.hawk.game.module.lianmenxhjz.battleroom.msg.XHJZQuitRoomMsg;
import com.hawk.game.module.lianmenxhjz.battleroom.player.IXHJZPlayer;
import com.hawk.game.module.lianmenxhjz.battleroom.player.XHJZPlayer;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;

public class XHJZPlayerModule extends PlayerModule {

	public XHJZPlayerModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		IXHJZPlayer gamer = XHJZRoomManager.getInstance().makesurePlayer(player.getId());
		if (gamer != null && gamer.getParent() != null) {
//			if(gamer.isAnchor()){
//				gamer.getParent().anchorJoinRoom(gamer);
//				return true;
//			}
			
			boolean needClean = gamer.getParent().getPlayer(player.getId()) == null || gamer.getParent().isGameOver();
			if (needClean) {
				gamer.getData().unLockOriginalData();
				gamer.setXhjzRoomId("");
				gamer.setXhjzState(null);
				XHJZRoomManager.getInstance().invalidate(gamer);
				return true;
			}
		}

		if (Objects.nonNull(gamer)) {
			XHJZPlayer tblyplayer = (XHJZPlayer) gamer;
			player.setXhjzState(XHJZState.GAMEING);
			player.setXhjzRoomId(tblyplayer.getParent().getId());
			tblyplayer.setSource(player);

			gamer.getParent().onPlayerLogin(gamer);
		} else {
			player.setXhjzRoomId("");
			player.setXhjzState(null);
			return true;
		}
		return super.onPlayerLogin();
	}

	@MessageHandler
	private void onJoinRoomMsg(XHJZJoinRoomMsg msg) {
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
		// 记录玩家兵力
//		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.XHJZ_JOIN_ROOM,
//				Params.valueOf("Army", armyStr));
		DungeonRedisLog.log(player.getId(), "roomId {} , army {}", player.getDYZZRoomId(), armyStr);
		msg.getBattleRoom().joinRoom(msg.getPlayer());
	}

	/** 结束, 记录胜负什么的 */
	@MessageHandler
	private void onQuitRoomMsg(XHJZQuitRoomMsg msg) {
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
		// 记录玩家兵力
//		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.XHJZ_QUIT_ROOM,
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
				.setMailId(MailId.XHJZ_BACK_SOLDIER)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				// .addSubTitles("叫爸爸!!!!")
				// .addContents("哥把兵让你带出副本, 叫爸爸!!!")
				.setRewards(items)
				.build());
	}

//	/** 主翻进房间 */
//	@ProtocolHandler(code = HP.code.XHJZ_ANCHOR_JOIN_REQ_VALUE)
//	private void onAnchorJoin(HawkProtocol protocol) {
//		if (!GameUtil.isOBPuidCtrlPlayer(player.getOpenId())) {
//			return;
//		}
//		
//		PBXHJZAnchorJoinReq req = protocol.parseProtocol(PBXHJZAnchorJoinReq.getDefaultInstance());
//		final String battleId = req.getBattleId();
//		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.XHJZAOGUAN_ROOM, battleId);
//		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.XHJZAOGUAN_ROOM).queryObject(roomXid);
//		XHJZBattleRoom room = null;
//		if (roomObj != null) {
//			room = (XHJZBattleRoom) roomObj.getImpl();
//		} else {
//			sendError(protocol.getType(), Status.Error.TIBERIUM_OB_NO_GAME);
//			return;
//		}
//		player.setXhjzRoomId(roomXid.getUUID());
//
//		IXHJZPlayer hp = new XHJZPlayer(player);
//		hp.setParent(room);
//		room.anchorJoinRoom(hp);
//	}
//	
//	@ProtocolHandler(code = HP.code2.XHJZ_LASTSNC_REQ_VALUE)
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
//		PBXHJZGameInfoSync resp = XHJZRoomManager.getInstance().getGaiLanResp(roomId);
//		
//		player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_LASTSNC_RESP, resp.toBuilder()));
//	}

}
