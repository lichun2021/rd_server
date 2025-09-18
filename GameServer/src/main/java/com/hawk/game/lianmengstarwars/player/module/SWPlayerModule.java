package com.hawk.game.lianmengstarwars.player.module;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.tuple.HawkTuple2;

import com.google.common.base.Joiner;
import com.hawk.game.config.SWCommandCenterCfg;
import com.hawk.game.config.SWHeadQuartersCfg;
import com.hawk.game.lianmengstarwars.SWRoomManager;
import com.hawk.game.lianmengstarwars.msg.SWJoinRoomMsg;
import com.hawk.game.lianmengstarwars.msg.SWQuitRoomMsg;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.lianmengstarwars.worldmarch.ISWWorldMarch;
import com.hawk.game.lianmengstarwars.worldmarch.submarch.ISWPassiveAlarmTriggerMarch;
import com.hawk.game.lianmengstarwars.worldmarch.submarch.ISWReportPushMarch;
import com.hawk.game.lianmengstarwars.worldpoint.SWCommandCenter;
import com.hawk.game.lianmengstarwars.worldpoint.SWHeadQuarters;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SW.PBLastVideoIndexReq;
import com.hawk.game.protocol.SW.PBLastVideoIndexResp;
import com.hawk.game.protocol.SW.PBSWVideoPackage;
import com.hawk.game.protocol.SW.PBVideoOfIndexReq;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchLoginPush;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.world.WorldMarch;
import com.hawk.log.Action;
import com.hawk.log.Source;

public class SWPlayerModule extends PlayerModule {

	public SWPlayerModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		ISWPlayer gamer = SWRoomManager.getInstance().makesurePlayer(player.getId());
		if (gamer != null && gamer.getParent() != null) {
			boolean needClean = gamer.getParent().getPlayer(player.getId()) == null || gamer.getParent().isGameOver();
			if (needClean) {
				gamer.getData().unLockOriginalData();
				gamer.setSwRoomId("");
				gamer.setSwState(null);
				SWRoomManager.getInstance().invalidate(gamer);
				return true;
			}
		}

		if (Objects.nonNull(gamer)) {
			gamer.getPush().syncPlayerWorldInfo();
			gamer.getPush().syncPlayerInfo();
			gamer.getPush().syncPlayerEffect();
			// 组装玩家自己的行军PB数据
			WorldMarchLoginPush.Builder builder = WorldMarchLoginPush.newBuilder();
			List<ISWWorldMarch> marchs = gamer.getParent().getPlayerMarches(gamer.getId());
			for (ISWWorldMarch worldMarch : marchs) {
				builder.addMarchs(worldMarch.toBuilder(WorldMarchRelation.SELF).build());

				WorldMarch march = worldMarch.getMarchEntity();
				if (march.getMarchType() == WorldMarchType.MASS_JOIN_VALUE) {
					ISWWorldMarch massMach = gamer.getParent().getMarch(march.getTargetId());
					// 这里添加个空判断。 存在队长解散后，队员找不到队长行军。所以这里可能问空。
					if (massMach != null) {
						builder.addMarchs(massMach.toBuilder(WorldMarchRelation.TEAM_LEADER).build());
					}
				}

				if (worldMarch instanceof ISWPassiveAlarmTriggerMarch) {
					((ISWPassiveAlarmTriggerMarch) worldMarch).pullAttackReport(player.getId());
				}
			}

			List<ISWWorldMarch> pointMs = gamer.getParent().getPointMarches(gamer.getPointId());
			for (ISWWorldMarch march : pointMs) {
				if (march instanceof ISWReportPushMarch) {
					((ISWReportPushMarch) march).pushAttackReport(gamer.getId());
				}
			}
			// 通知客户端
			gamer.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCHS_PUSH, builder));
			gamer.moveCityCDSync();

		}
		return super.onPlayerLogin();
	}
	
	/**预览*/
	@ProtocolHandler(code = HP.code.SW_VISITTTTTT_C_VALUE)
	private void onVisitttttttt(HawkProtocol protocol) {
		WorldPointSync.Builder respbuilder = WorldPointSync.newBuilder();
		{
			SWHeadQuartersCfg buildcfg = SWHeadQuarters.getCfg();
			int index = 0;
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				WorldPointPB.Builder builder = WorldPointPB.newBuilder();
				builder.setPointX(rp.first);
				builder.setPointY(rp.second);
				builder.setPointType(WorldPointType.SW_HEADQUARTERS);
//				builder.setPlayerId(getPlayerId());
//				builder.setPlayerName(getPlayerName());
//				builder.setGuildId(getGuildId());
//				builder.setGuildTag(getGuildTag());
//				builder.setGuildFlag(getGuildFlag());

				builder.setManorState(0); // /**中立*/(0),/**占领中*(1),/**已占*/(2);
				builder.setMonsterId(index); // 按配置点顺序出生序号 0 , 1
				// 城点保护时间
				builder.setProtectedEndTime(Long.MAX_VALUE);
				respbuilder.addPoints(builder);
				
				index++;
			}
		}

		{
			SWCommandCenterCfg buildcfg = SWCommandCenter.getCfg();
			int index = 0;
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				WorldPointPB.Builder builder = WorldPointPB.newBuilder();
				builder.setPointX(rp.first);
				builder.setPointY(rp.second);
				builder.setPointType(WorldPointType.SW_COMMAND_CENTER);
				builder.setManorState(0); // /**中立*/(0),/**占领中*(1),/**已占*/(2);
				builder.setMonsterId(index); // 按配置点顺序出生序号 0 , 1
				// 城点保护时间
				builder.setProtectedEndTime(Long.MAX_VALUE);
				respbuilder.addPoints(builder);
				index++;
			}
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, respbuilder));
		player.responseSuccess(protocol.getType());
	}

	/**最新录像包号*/
	@ProtocolHandler(code = HP.code.SW_LAST_VIDEO_INDEX_C_VALUE)
	private void onGetLastBattleVideoIndex(HawkProtocol protocol) {
		PBLastVideoIndexReq req = protocol.parseProtocol(PBLastVideoIndexReq.getDefaultInstance());
		final String battleId = req.getBattleId();
		Integer lastVideoIndex = SWRoomManager.getInstance().videoLastIndex(battleId);

		PBLastVideoIndexResp.Builder resp = PBLastVideoIndexResp.newBuilder();
		resp.setBattleId(battleId);
		if (lastVideoIndex != null) {
			resp.setHasVideo(true);
			resp.setVideoIndex(lastVideoIndex.intValue());
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SW_LAST_VIDEO_INDEX_S, resp));
	}

	/**
	 * 指定序号录像包
	 */
	@ProtocolHandler(code = HP.code.SW_VIDEO_OF_INDEX_C_VALUE)
	private void onGetVideoOfIndex(HawkProtocol protocol) {
		PBVideoOfIndexReq req = protocol.parseProtocol(PBVideoOfIndexReq.getDefaultInstance());
		final String battleId = req.getBattleId();
		final int index = req.getIndex();

		Optional<PBSWVideoPackage> packOp = SWRoomManager.getInstance().videoPackageOfIndex(battleId, index);
		if (!packOp.isPresent()) {
			sendError(protocol.getType(), Status.Error.SW_NO_NICEO);
			return;
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SW_VIDEO_OF_INDEX_S, packOp.get().toBuilder()));
	}

	@MessageHandler
	private void onJoinRoomMsg(SWJoinRoomMsg msg) {
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
		// 记录玩家兵力
//		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.SW_JOIN_ROOM,
//				Params.valueOf("Army", armyStr));
		DungeonRedisLog.log(player.getId(), "roomId {} , army {}", player.getSwRoomId(), armyStr);
		msg.getBattleRoom().joinRoom(msg.getPlayer());
	}

	/** 结束, 记录胜负什么的 */
	@MessageHandler
	private void onQuitRoomMsg(SWQuitRoomMsg msg) {
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
		DungeonRedisLog.log(player.getId(), "roomId {} , army {}", msg.getRoomId(), armyStr);
		// Map<String, String> csmap = RedisProxy.getInstance().jbsTakeOutAllCreateSoldier(player.getId());
		// if (csmap.isEmpty()) {
		// return;
		// }
		// String items = "";
		// for (Entry<String, String> ent : csmap.entrySet()) {
		// items += String.format("70000_%s_%s,", ent.getKey(), ent.getValue());
		// }
		//
		// SystemMailService.getInstance().sendMail(MailParames.newBuilder()
		// .setPlayerId(player.getId())
		// .setMailId(MailId.SW_BACK_SOLDIER)
		// .setAwardStatus(MailRewardStatus.NOT_GET)
		// // .addSubTitles("叫爸爸!!!!")
		// // .addContents("哥把兵让你带出副本, 叫爸爸!!!")
		// .setRewards(items)
		// .build());
	}

}
