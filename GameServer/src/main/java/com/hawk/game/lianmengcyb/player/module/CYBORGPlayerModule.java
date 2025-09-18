package com.hawk.game.lianmengcyb.player.module;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.obj.HawkObjBase;
import org.hawk.xid.HawkXID;

import com.google.common.base.Joiner;
import com.hawk.game.GsApp;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.lianmengcyb.CYBORGRoomManager;
import com.hawk.game.lianmengcyb.msg.CYBORGJoinRoomMsg;
import com.hawk.game.lianmengcyb.msg.CYBORGQuitRoomMsg;
import com.hawk.game.lianmengcyb.player.CYBORGPlayer;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.CYBORG.PBCYBORGAnchorJoinReq;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;

public class CYBORGPlayerModule extends PlayerModule {

	public CYBORGPlayerModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		ICYBORGPlayer gamer = CYBORGRoomManager.getInstance().makesurePlayer(player.getId());
		if (gamer != null && gamer.getParent() != null) {
			if(gamer.isAnchor()){
				gamer.getParent().anchorJoinRoom(gamer);
				return true;
			}
			
			boolean needClean = gamer.getParent().getPlayer(player.getId()) == null || gamer.getParent().isGameOver();
			if (needClean) {
				gamer.getData().unLockOriginalData();
				gamer.setCYBORGRoomId("");
				gamer.setCYBORGState(null);
				CYBORGRoomManager.getInstance().invalidate(gamer);
				return true;
			}
		}

		if (Objects.nonNull(gamer)) {
			gamer.getParent().onPlayerLogin(gamer);
		}
		return super.onPlayerLogin();
	}

	@MessageHandler
	private void onJoinRoomMsg(CYBORGJoinRoomMsg msg) {
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
//		// 记录玩家兵力
//		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.CYBORG_JOIN_ROOM,
//				Params.valueOf("Army", armyStr));
		DungeonRedisLog.log(player.getId(), "roomId {} , army {}", player.getDYZZRoomId(), armyStr);
		
		msg.getBattleRoom().joinRoom(msg.getPlayer());
	}

	/** 结束, 记录胜负什么的 */
	@MessageHandler
	private void onQuitRoomMsg(CYBORGQuitRoomMsg msg) {
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
		// 记录玩家兵力
//		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.CYBORG_QUIT_ROOM,
//				Params.valueOf("Army", armyStr));
		DungeonRedisLog.log(player.getId(), "roomId {} , army {}", player.getDYZZRoomId(), armyStr);
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
				.setMailId(MailId.CYBORG_BACK_SOLDIER)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				// .addSubTitles("叫爸爸!!!!")
				// .addContents("哥把兵让你带出副本, 叫爸爸!!!")
				.setRewards(items)
				.build());
	}

	/** 主翻进房间 */
	@ProtocolHandler(code = HP.code.CYBORG_ANCHOR_JOIN_REQ_VALUE)
	private void onAnchorJoin(HawkProtocol protocol) {
		if (!GameUtil.isOBPuidCtrlPlayer(player.getOpenId())) {
			return;
		}
		
		PBCYBORGAnchorJoinReq req = protocol.parseProtocol(PBCYBORGAnchorJoinReq.getDefaultInstance());
		final String battleId = req.getBattleId();
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.CYBORGAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.CYBORGAOGUAN_ROOM).queryObject(roomXid);
		CYBORGBattleRoom room = null;
		if (roomObj != null) {
			room = (CYBORGBattleRoom) roomObj.getImpl();
		} else {
			sendError(protocol.getType(), Status.Error.TIBERIUM_OB_NO_GAME);
			return;
		}
		player.setCYBORGRoomId(roomXid.getUUID());

		ICYBORGPlayer hp = new CYBORGPlayer(player);
		hp.setParent(room);
		room.anchorJoinRoom(hp);
	}

}
