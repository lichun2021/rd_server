package com.hawk.game.lianmengjunyan.player.module;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.hawk.annotation.MessageHandler;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.google.common.base.Joiner;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.lianmengjunyan.LMJYRoomManager;
import com.hawk.game.lianmengjunyan.msg.LMJYGameOverMsg;
import com.hawk.game.lianmengjunyan.msg.LMJYJoinRoomMsg;
import com.hawk.game.lianmengjunyan.msg.LMJYQuitRoomMsg;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.lianmengjunyan.worldmarch.ILMJYWorldMarch;
import com.hawk.game.lianmengjunyan.worldmarch.submarch.ILMJYPassiveAlarmTriggerMarch;
import com.hawk.game.lianmengjunyan.worldmarch.submarch.ILMJYReportPushMarch;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.WarCollege.TeamPlayerOper;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchLoginPush;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.warcollege.WarCollegeInstanceService;
import com.hawk.game.warcollege.model.WarCollegeTeam;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.log.Action;
import com.hawk.log.Source;

public class LMJYPlayerModule extends PlayerModule {

	public LMJYPlayerModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		ILMJYPlayer gamer = LMJYRoomManager.getInstance().makesurePlayer(player.getId());
		if (Objects.nonNull(gamer) && gamer.getLmjyState() == PState.GAMEING) {
			gamer.getParent().sync();
			gamer.getPush().syncPlayerWorldInfo();

			// 组装玩家自己的行军PB数据
			WorldMarchLoginPush.Builder builder = WorldMarchLoginPush.newBuilder();
			List<ILMJYWorldMarch> marchs = gamer.getParent().getPlayerMarches(gamer.getId());
			for (ILMJYWorldMarch worldMarch : marchs) {
				builder.addMarchs(worldMarch.toBuilder(WorldMarchRelation.SELF).build());

				WorldMarch march = worldMarch.getMarchEntity();
				if (march.getMarchType() == WorldMarchType.MASS_JOIN_VALUE) {
					ILMJYWorldMarch massMach = gamer.getParent().getMarch(march.getTargetId());
					// 这里添加个空判断。 存在队长解散后，队员找不到队长行军。所以这里可能问空。
					if (massMach != null) {
						builder.addMarchs(massMach.toBuilder(WorldMarchRelation.TEAM_LEADER).build());
					}
				}

				if (worldMarch instanceof ILMJYPassiveAlarmTriggerMarch) {
					((ILMJYPassiveAlarmTriggerMarch) worldMarch).pullAttackReport(player.getId());
				}
			}

			List<ILMJYWorldMarch> pointMs = gamer.getParent().getPointMarches(gamer.getPointId());
			for (ILMJYWorldMarch march : pointMs) {
				if (march instanceof ILMJYReportPushMarch) {
					((ILMJYReportPushMarch) march).pushAttackReport(gamer.getId());
				}
			}
			// 通知客户端
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCHS_PUSH, builder));

		} 
		if (Objects.isNull(gamer) || gamer.getParent().getOverTime() + 5000 < HawkTime.getMillisecond()) {
			WarCollegeTeam team = WarCollegeInstanceService.getInstance().getWarCollegeTeamByPlayerId(player.getId());
			if (Objects.nonNull(team)) {
				HawkLog.logPrintln("fix team {} create {}", team.getBattleId(), team.getCreateTime());
				WarCollegeInstanceService.getInstance().onTeamQuit(player, TeamPlayerOper.TEAM_PLAYER_OVER_INSTANCE);
			}

			player.setLmjyRoomId(null);
			player.setLmjyState(null);
		}
		if (!player.isInDungeonMap()) {
			player.getData().getDataCache().clearLockKey();
		}
		return super.onPlayerLogin();
	}

	/** 结束, 记录胜负什么的 */
	@MessageHandler
	private void onGameOverMsg(LMJYGameOverMsg msg) {
		// 记录玩家兵力
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
		DungeonRedisLog.log(player.getId(), "{}", armyStr);
//		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.LMJY_QUIT_ROOM,
//				Params.valueOf("Army", armyStr));

		Map<String, String> csmap = LocalRedis.getInstance().lmjyTakeOutAllCreateSoldier(player.getId());
		if (csmap.isEmpty()) {
			return;
		}
		String items = "";
		for (Entry<String, String> ent : csmap.entrySet()) {
			items += String.format("70000_%s_%s,", ent.getKey(), ent.getValue());
		}

		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.LMJY_BACK_SOLDIER)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				// .addSubTitles("叫爸爸!!!!")
				// .addContents("哥把兵让你带出副本, 叫爸爸!!!")
				.setRewards(items)
				.build());
	}

	@MessageHandler
	private void onJoinRoomMsg(LMJYJoinRoomMsg msg) {
		if (WorldMarchService.getInstance().getPlayerMarchCount(player.getId()) > 0) {// 有行军不能加入
			player.sendError(HP.code.LMJY_JOIN_ROOM_REQ_VALUE, Status.Error.LMJY_HAS_PLYAER_MARCH_VALUE, 0);
		}
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
//		// 记录玩家兵力
//		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.LMJY_JOIN_ROOM,
//				Params.valueOf("Army", armyStr));
		DungeonRedisLog.log(player.getId(), "{}", armyStr);

		msg.getBattleRoom().joinRoom(msg.getPlayer());
	}

	@MessageHandler
	private void onQuitRoomMsg(LMJYQuitRoomMsg msg) {
		ILMJYPlayer hp = msg.getPlayer();
		if (Objects.nonNull(hp.getPush())) {
			hp.getPush().pushGameOver();
		} else {
			hp.quitGame();
		}
	}
}
