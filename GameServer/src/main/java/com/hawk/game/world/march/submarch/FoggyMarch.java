package com.hawk.game.world.march.submarch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.hawk.game.invoker.guildBackActivity.GuildBackDropInvoker;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.service.ActivityService;
import com.hawk.game.service.GuildService;
import com.hawk.gamelib.GameConst;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.AttackFoggyEvent;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.FoggyFortressCfg;
import com.hawk.game.entity.StatisticsEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisKey;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.lianmengxzq.task.XZQTicketAddInvoker;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.msg.SuperSoldierTriggeTaskMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SuperSoldier.SupersoldierTaskType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.protocol.XZQ.PBXZQStatus;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.MailRewards;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventAttackFoggy;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.util.GsConst.StatisticDataType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldFoggyFortressService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.Source;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

/**
 * 幽灵基地行军
 * @author golden
 *
 */
public interface FoggyMarch extends BasedMarch {

	@Override
	default void onMarchReach(Player leader) {
		// 队长行军
		WorldMarch leaderMarch = this.getMarchEntity();
		// 目标点
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(leaderMarch.getTerminalId());

		// 目标点错误
		if (worldPoint == null || worldPoint.getPointType() != WorldPointType.FOGGY_FORTRESS_VALUE) {
			WorldFoggyFortressService.getInstance().sendPointDisappearMail(this, MailId.WORLD_FOGGY_MARCH_POINT_DISAPPEAR);
			returnMarchList(getMassMarchList(this));
			return;
		}

		// 目标点正在活动中
		if (WorldFoggyFortressService.getInstance().checkPointIsInActive(worldPoint.getId())) {
			WorldFoggyFortressService.getInstance().sendPointDisappearMail(this, MailId.WORLD_FOGGY_IN_ACTIVE);
			returnMarchList(getMassMarchList(this));
			return;
		}

		/**********************    战斗数据组装及战斗***************************/
		// 进攻方玩儿家
		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(leader);

		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);

		// 填充参与集结信息
		Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			atkPlayers.add(GlobalData.getInstance().makesurePlayer(massJoinMarch.getPlayerId()));
			atkMarchs.add(massJoinMarch);
		}

		// 战斗信息
		PveBattleIncome battleIncome = BattleService.getInstance().initFoggyBattleData(BattleConst.BattleType.ATTACK_FOGGY, worldPoint, atkPlayers, atkMarchs);

		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		/**********************战斗数据组装及战斗***************************/

		// 战斗结果处理
		MailRewards mailRewards = doFoggyBattleResult(battleOutcome, atkPlayers, worldPoint.getMonsterId());
		// 攻击方玩家部队
		List<ArmyInfo> mergAllPlayerArmy = WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk());
		// 发送战斗结果
		WorldMarchService.getInstance().sendBattleResultInfo(this, battleOutcome.isAtkWin(), mergAllPlayerArmy, new ArrayList<ArmyInfo>(), battleOutcome.isAtkWin());
		// 据点PVE战斗邮件发放
		FightMailService.getInstance().sendPveFightMail(BattleConst.BattleType.ATTACK_FOGGY, battleIncome, battleOutcome, mailRewards);

		// 日志
		FoggyFortressCfg foggyCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, worldPoint.getMonsterId());
		List<String> atkPlayerIds = new ArrayList<>();
		for (Player player : atkPlayers) {
			BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.ATK_FOGGY,
					Params.valueOf("x", worldPoint.getX()),
					Params.valueOf("y", worldPoint.getY()),
					Params.valueOf("foggyId", foggyCfg.getId()),
					Params.valueOf("foggyLvl", foggyCfg.getLevel()),
					Params.valueOf("isLeader", player.getId().equals(leader.getId())),
					Params.valueOf("isWin", battleOutcome.isAtkWin()),
					Params.valueOf("playerNum", atkPlayers.size()));
			
			LogUtil.logAttackFoggy(player, worldPoint.getX(), worldPoint.getY(), foggyCfg.getId(), foggyCfg.getLevel(), player.getId().equals(leader.getId()),
					battleOutcome.isAtkWin(), atkPlayers.size());
			atkPlayerIds.add(player.getId());
		}
		
		// 行军返回
		WorldMarchService.getInstance().onMarchReturn(this, battleOutcome.getAftArmyMapAtk().get(this.getMarchEntity().getPlayerId()), 0);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			WorldMarchService.getInstance().onMarchReturn(massJoinMarch, battleOutcome.getAftArmyMapAtk().get(massJoinMarch.getMarchEntity().getPlayerId()), 0);
		}
		// 移除世界点
		if (battleOutcome.isAtkWin()) {
			WorldFoggyFortressService.getInstance().notifyFoggyFortressKilled(worldPoint.getId());
			for (Player atkPlayer : atkPlayers) {
				StatisticsEntity statisticsEntity = atkPlayer.getData().getStatisticsEntity();
				statisticsEntity.addCommonStatisData(StatisticDataType.ATK_GHOST_TOTAL_TODAY, 1);
			}
		}
		
		//pve过后抛事件更新玩家信息
		boolean isMass = this.isMassMarch();
		if (isMass) {
			ActivityService.getInstance().dealMsg(GameConst.MsgId.ON_GUILD_BACK_DROP, new GuildBackDropInvoker(atkPlayerIds));
			for (Player atkPlayer : atkPlayers) {
				postEventAfterPve(atkPlayer, battleOutcome, this.getMarchType(), 0);
				MissionManager.getInstance().postMsg(atkPlayer, new EventAttackFoggy(foggyCfg.getId(), foggyCfg.getLevel(), battleOutcome.isAtkWin(), this.isMassMarch(), getMarchId()));
				MissionManager.getInstance().postSuperSoldierTaskMsg(atkPlayer, new SuperSoldierTriggeTaskMsg(SupersoldierTaskType.ATTACK_YUTI_BASE, 1));
				ActivityManager.getInstance().postEvent(new AttackFoggyEvent(atkPlayer.getId(), battleOutcome.isAtkWin(), foggyCfg.getLevel(), isMass));
				StatisticsEntity statisticsEntity = atkPlayer.getData().getStatisticsEntity();
				statisticsEntity.addCommonStatisData(StatisticDataType.GROUP_TOTAL_TODAY, 1);
			}
		}
		
		// 添加行军到达标志位
		this.getMarchEntity().addWorldMarchProcMask(GsConst.MarchProcMask.IS_MARCHREACH);
		for (IWorldMarch march : massJoinMarchs) {
			march.getMarchEntity().addWorldMarchProcMask(GsConst.MarchProcMask.IS_MARCHREACH);
		}
	}
	
	/**
	 * 战斗结果处理
	 * @param outCome
	 * @param atkPlayers
	 * @param foggyId
	 */
	default MailRewards doFoggyBattleResult(BattleOutcome outCome, List<Player> atkPlayers, int foggyId) {
		// 奖励展示。 只用于邮件里显示。
		MailRewards mailRewards = new MailRewards();
		if (!outCome.isAtkWin() || !this.isMassMarch()) {
			return mailRewards;
		}
		
		boolean mass = this.isMassMarch(), allianceGift = false;
		int guildAwardTotal = 0;
		String redisKey = "";
		if (mass) {
			redisKey = RedisKey.MASS_FOGGY_ALLI_GIFT + ":" + atkPlayers.get(0).getGuildId() + ":" + HawkTime.getYyyyMMddIntVal();
			String result = RedisProxy.getInstance().getRedisSession().getString(redisKey);
			guildAwardTotal = HawkOSOperator.isEmptyString(result) ? 0 : Integer.parseInt(result);
		}
		
		FoggyFortressCfg foggyCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
		for (Player atkPlayer : atkPlayers) {
			int eff642 = atkPlayer.getEffect().getEffVal(Const.EffType.LIFE_TIME_CARD_642);
			//发起集结
			int massFoggyWinTimes = atkPlayer.getData().getDailyDataEntity().getMassAtkFoggyWinTimes();
			int massMaxTimes = ConstProperty.getInstance().getStartAssembleRewardGetLimit() + eff642;
			//参与集结
			int joinFoggyWinTimes = atkPlayer.getData().getDailyDataEntity().getJoinAtkFoggyWinTimes();
			int joinMaxTimes = ConstProperty.getInstance().getAssembleRewardGetLimit();
			
			AwardItems awardItems = AwardItems.valueOf();
			boolean isLeader = atkPlayers.get(0).getId().equals(atkPlayer.getId());
			//队长发起集结的人
			if (isLeader && massFoggyWinTimes < massMaxTimes) {
				atkPlayer.getData().getDailyDataEntity().addMassAtkFoggyWinTimes(1);
				awardItems.addAward(foggyCfg.getStartAssembleReward());
				//集结联盟礼物 
				if (foggyCfg.getAllianceGift() > 0 && guildAwardTotal < ConstProperty.getInstance().getMassFoggyAllianceGiftLimit()) {
					allianceGift = true;
				}
				//发起集结奖励
				MailParames.Builder mailParames = MailParames.newBuilder()
						.setMailId(MailId.MASS_FOGGY_REWARD)
						.setPlayerId(atkPlayer.getId())
						.setRewards(awardItems.toString())
						.setAwardStatus(MailRewardStatus.NOT_GET);
				SystemMailService.getInstance().sendMail(mailParames.build());
			}
			
			//参与集结的人，队长也算参与了集结
			if (joinFoggyWinTimes < joinMaxTimes) {
				atkPlayer.getData().getDailyDataEntity().addJoinAtkFoggyWinTimes(1);
				//集结参与奖励
				AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, foggyCfg.getAssembleReward());
				if (awardCfg != null) {
					AwardItems joinAward = awardCfg.getRandomAward();
					awardItems.appendAward(joinAward);
					MailParames.Builder mailParames = MailParames.newBuilder()
							.setMailId(MailId.JOIN_FOGGY_REWARD)
							.setPlayerId(atkPlayer.getId())
							.setRewards(joinAward.toString())
							.setAwardStatus(MailRewardStatus.NOT_GET);
					SystemMailService.getInstance().sendMail(mailParames.build());
				}
			}
			
			if (!awardItems.getAwardItems().isEmpty()) {
				atkPlayer.getPush().syncPlayerInfo();
			}
			mailRewards.addSelfRewards(atkPlayer.getId(), awardItems.getAwardItems());
		}
		
		//发放集结联盟礼物
		if (allianceGift) {
			RedisProxy.getInstance().getRedisSession().increaseBy(redisKey, 1, 3600 * 24);
			GuildService.getInstance().bigGift(atkPlayers.get(0).getGuildId()).addSmailGift(foggyCfg.getAllianceGift(), false);
		}
		
		//小站区门票发放
		if(XZQService.getInstance().getState() == PBXZQStatus.XZQ_SIGNUP){
			XZQService.getInstance().dealMsg(MsgId.XZQ_TICKET_ADD, new XZQTicketAddInvoker(atkPlayers));
		}
		
		return mailRewards;
	}
	
	/**
	 * 获取被动方联盟战争界面信息
	 */
	@Override
	default GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		
		int terminalId = this.getMarchEntity().getTerminalId();
		int[] pos = GameUtil.splitXAndY(terminalId);
		builder.setPointType(WorldPointType.FOGGY_FORTRESS);
		builder.setX(pos[0]);
		builder.setY(pos[1]);

		int targetId = Integer.parseInt(this.getMarchEntity().getTargetId());
		builder.setMonsterId(targetId);
		return builder;
	}
}
