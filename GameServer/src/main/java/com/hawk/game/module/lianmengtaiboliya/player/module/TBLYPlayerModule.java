package com.hawk.game.module.lianmengtaiboliya.player.module;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.hawk.game.service.tiberium.TWGuildData;
import com.hawk.game.service.tiberium.TWRoomData;
import com.hawk.game.service.tiberium.TiberiumLeagueWarService;
import com.hawk.game.service.tiberium.TiberiumWarService;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.obj.HawkObjBase;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;

import com.google.common.base.Joiner;
import com.hawk.game.GsApp;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.module.lianmengtaiboliya.msg.TBLYJoinRoomMsg;
import com.hawk.game.module.lianmengtaiboliya.msg.TBLYQuitRoomMsg;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.player.TBLYPlayer;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.TBLY.PBTBLYAnchorJoinReq;
import com.hawk.game.protocol.TBLY.PBTBLYGameInfoSync;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;

public class TBLYPlayerModule extends PlayerModule {

	public TBLYPlayerModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		ITBLYPlayer gamer = TBLYRoomManager.getInstance().makesurePlayer(player.getId());
		if (gamer != null && gamer.getParent() != null) {
			if(gamer.isAnchor()){
				gamer.getParent().anchorJoinRoom(gamer);
				return true;
			}
			
			boolean needClean = gamer.getParent().getPlayer(player.getId()) == null || gamer.getParent().isGameOver();
			if (needClean) {
				gamer.getData().unLockOriginalData();
				gamer.setTBLYRoomId("");
				gamer.setTBLYState(null);
				TBLYRoomManager.getInstance().invalidate(gamer);
				return true;
			}
		}

		if (Objects.nonNull(gamer)) {
			TBLYPlayer tblyplayer = (TBLYPlayer) gamer;
			player.setTBLYState(TBLYState.GAMEING);
			player.setTBLYRoomId(tblyplayer.getParent().getId());
			tblyplayer.setSource(player);

			gamer.getParent().onPlayerLogin(gamer);
		} else {
			player.setTBLYRoomId("");
			player.setTBLYState(null);
			return true;
		}
		return super.onPlayerLogin();
	}

	@MessageHandler
	private void onJoinRoomMsg(TBLYJoinRoomMsg msg) {
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
		// 记录玩家兵力
//		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.TBLY_JOIN_ROOM,
//				Params.valueOf("Army", armyStr));
		DungeonRedisLog.log(player.getId(), "roomId {} , army {}", player.getDYZZRoomId(), armyStr);
		msg.getBattleRoom().joinRoom(msg.getPlayer());
	}

	/** 结束, 记录胜负什么的 */
	@MessageHandler
	private void onQuitRoomMsg(TBLYQuitRoomMsg msg) {
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
		// 记录玩家兵力
//		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.TBLY_QUIT_ROOM,
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
				.setMailId(MailId.TBLY_BACK_SOLDIER)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				// .addSubTitles("叫爸爸!!!!")
				// .addContents("哥把兵让你带出副本, 叫爸爸!!!")
				.setRewards(items)
				.build());
	}

	/** 主翻进房间 */
	@ProtocolHandler(code = HP.code.TBLY_ANCHOR_JOIN_REQ_VALUE)
	private void onAnchorJoin(HawkProtocol protocol) {
		if (!GameUtil.isOBPuidCtrlPlayer(player.getOpenId())) {
			return;
		}
		
		PBTBLYAnchorJoinReq req = protocol.parseProtocol(PBTBLYAnchorJoinReq.getDefaultInstance());
		final String battleId = req.getBattleId();
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.TBLYAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.TBLYAOGUAN_ROOM).queryObject(roomXid);
		TBLYBattleRoom room = null;
		if (roomObj != null) {
			room = (TBLYBattleRoom) roomObj.getImpl();
		} else {
			sendError(protocol.getType(), Status.Error.TIBERIUM_OB_NO_GAME);
			return;
		}
		player.setTBLYRoomId(roomXid.getUUID());

		ITBLYPlayer hp = new TBLYPlayer(player);
		hp.setParent(room);
		room.anchorJoinRoom(hp);
	}
	
	@ProtocolHandler(code = HP.code2.TBLY_LASTSNC_REQ_VALUE)
	private void onGetSyncPb(HawkProtocol protocol) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}

		boolean isInLeaguaWar = TiberiumLeagueWarService.getInstance().isJointLeaguaWar(guildId);
		int termId = 0;
		if (isInLeaguaWar) {
			termId = TiberiumLeagueWarService.getInstance().getActivityInfo().getMark();
		} else {
			termId = TiberiumWarService.getInstance().getActivityData().getTermId();
		}
		TWGuildData guildData = RedisProxy.getInstance().getTWGuildData(guildId, termId);
		if (guildData == null) {
			return;
		}
		String roomId = guildData.getRoomId();
		if (HawkOSOperator.isEmptyString(guildData.getRoomId())) {
			return;
		}
		PBTBLYGameInfoSync resp = TBLYRoomManager.getInstance().getGaiLanResp(roomId);
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.TBLY_LASTSNC_RESP, resp.toBuilder()));
	}

}
