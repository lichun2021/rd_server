package com.hawk.game.module.lianmengXianquhx.player.module;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.hawk.annotation.MessageHandler;

import com.google.common.base.Joiner;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengXianquhx.XQHXConst.XQHXState;
import com.hawk.game.module.lianmengXianquhx.XQHXRoomManager;
import com.hawk.game.module.lianmengXianquhx.msg.XQHXJoinRoomMsg;
import com.hawk.game.module.lianmengXianquhx.msg.XQHXQuitRoomMsg;
import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;
import com.hawk.game.module.lianmengXianquhx.player.XQHXPlayer;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;

public class XQHXPlayerModule extends PlayerModule {

	public XQHXPlayerModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		IXQHXPlayer gamer = XQHXRoomManager.getInstance().makesurePlayer(player.getId());
		if (gamer != null && gamer.getParent() != null) {
			
			boolean needClean = gamer.getParent().getPlayer(player.getId()) == null || gamer.getParent().isGameOver();
			if (needClean) {
				gamer.getData().unLockOriginalData();
				gamer.setXQHXRoomId("");
				gamer.setXQHXState(null);
				XQHXRoomManager.getInstance().invalidate(gamer);
				return true;
			}
		}

		if (Objects.nonNull(gamer)) {
			XQHXPlayer tblyplayer = (XQHXPlayer) gamer;
			player.setXQHXState(XQHXState.GAMEING);
			player.setXQHXRoomId(tblyplayer.getParent().getId());
			tblyplayer.setSource(player);

			gamer.getParent().onPlayerLogin(gamer);
		} else {
			player.setXQHXRoomId("");
			player.setXQHXState(null);
			return true;
		}
		return super.onPlayerLogin();
	}

	@MessageHandler
	private void onJoinRoomMsg(XQHXJoinRoomMsg msg) {
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
		// 记录玩家兵力
//		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.XQHX_JOIN_ROOM,
//				Params.valueOf("Army", armyStr));
		DungeonRedisLog.log(player.getId(), "roomId {} , army {}", player.getDYZZRoomId(), armyStr);
		msg.getBattleRoom().joinRoom(msg.getPlayer());
	}

	/** 结束, 记录胜负什么的 */
	@MessageHandler
	private void onQuitRoomMsg(XQHXQuitRoomMsg msg) {
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
		// 记录玩家兵力
//		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.XQHX_QUIT_ROOM,
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
				.setMailId(MailId.XQHX_BACK_SOLDIER)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				// .addSubTitles("叫爸爸!!!!")
				// .addContents("哥把兵让你带出副本, 叫爸爸!!!")
				.setRewards(items)
				.build());
	}

	
//	@ProtocolHandler(code = HP.code2.XQHX_LASTSNC_REQ_VALUE)
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
//		PBXQHXGameInfoSync resp = XQHXRoomManager.getInstance().getGaiLanResp(roomId);
//		
//		player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_LASTSNC_RESP, resp.toBuilder()));
//	}

}
