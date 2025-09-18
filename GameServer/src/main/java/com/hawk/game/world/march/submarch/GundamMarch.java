package com.hawk.game.world.march.submarch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.MachineAwakeAttackEvent;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst.BattleType;
import com.hawk.game.config.WorldGundamCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.entity.StatisticsEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.invoker.MarchVitReturnBackMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.Mail.MonsterMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.util.GsConst.StatisticDataType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldGundamService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 高达行军
 * @author golden
 *
 */
public interface GundamMarch extends BasedMarch {
	
	@Override
	default void onMarchReach(Player leader) {
		// 行军
		WorldMarch leaderMarch = getMarchEntity();
		// 目标点
		int terminalId = leaderMarch.getTerminalId();
		// 目标高达
		int gundamId = Integer.valueOf(leaderMarch.getTargetId());
		// 点和怪信息
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(terminalId);

		// 点为空
		if (point == null) {
			gundamMarchReturn();
			sendPointErrorMail(leader);
			WorldMarchService.logger.info("attackGundamMarch reach, point null, terminalId:{}", terminalId);
			return;
		}

		// 非野怪点
		if (point.getPointType() != WorldPointType.GUNDAM_VALUE) {
			gundamMarchReturn();
			sendPointErrorMail(leader);
			WorldMarchService.logger.error("attackGundamMarch reach, point not gundam, terminalId:{}", terminalId);
			return;
		}

		// 点改变
		if (point.getMonsterId() != gundamId) {
			gundamMarchReturn();
			sendPointErrorMail(leader);
			WorldMarchService.logger.error("attackGundamMarch reach, point has changed, terminalId:{}", terminalId);
			return;
		}

		// 高达配置
		WorldGundamCfg gundamCfg = HawkConfigManager.getInstance().getConfigByKey(WorldGundamCfg.class, gundamId);
		if (gundamCfg == null) {
			gundamMarchReturn();
			sendPointErrorMail(leader);
			WorldMarchService.logger.error("attackGundamMarch reach, monsterCfg null, gundamId:{}", gundamId);
			return;
		}

		// 高达血量
		int beforeBlood = point.getRemainBlood();

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
		
		WorldGundamCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldGundamCfg.class, point.getMonsterId());
		
		// 战斗数据输入
		PveBattleIncome battleIncome = BattleService.getInstance().initGundamBattleData(BattleType.ATTACK_GUNDAM_PVE, point.getId(), cfg.getId(), cfg.getArmyList(), atkMarchs);
		
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		/**********************战斗数据组装及战斗***************************/
		
		// 战斗结果处理
		doBattleResult(leader, point, gundamCfg, atkPlayers, battleOutcome);
		
		// 发送战斗结果 集结野怪只有胜利
		WorldMarchService.getInstance().sendBattleResultInfo(this, true, WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk()), new ArrayList<ArmyInfo>(), point.getRemainBlood() <= 0);
		
		// 行军返回
		WorldMarchService.getInstance().onMarchReturn(this, battleOutcome.getAftArmyMapAtk().get(this.getMarchEntity().getPlayerId()), 0);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			WorldMarchService.getInstance().onMarchReturn(massJoinMarch, battleOutcome.getAftArmyMapAtk().get(massJoinMarch.getMarchEntity().getPlayerId()), 0);
		}
		
		// 战斗胜利，移除点
		if (point.getRemainBlood() <= 0) {
			if (this.isMassMarch()) {
				String guildTag = GuildService.getInstance().getGuildTag(this.getPlayer().getGuildId());
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.WORLD_GUNDAM_GUILD_KILLED, null, guildTag, point.getX(), point.getY());
			} else {
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.WORLD_GUNDAM_PLAYER_KILLED, null, this.getPlayer().getName(), point.getX(), point.getY());
			}
			
			WorldGundamService.getInstance().notifyGundamKilled(point.getId());
		}

		boolean mass = this.isMassMarch();
		for (Player player : atkPlayers) {
			if (mass) {
				StatisticsEntity statisticsEntity = player.getData().getStatisticsEntity();
				statisticsEntity.addCommonStatisData(StatisticDataType.GROUP_TOTAL_TODAY, 1);
			}
			BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.GUNDAM_MARCH,
					Params.valueOf("pointId", point.getId()),
					Params.valueOf("x", point.getX()),
					Params.valueOf("y", point.getY()),
					Params.valueOf("gundamId", gundamId),
					Params.valueOf("isKill", point.getRemainBlood() <= 0),
					Params.valueOf("isLeader", player.getId().equals(leader.getId())),
					Params.valueOf("isMass", mass),
					Params.valueOf("beforeBlood", beforeBlood),
					Params.valueOf("afterBlood", point.getRemainBlood()));
		}

		// 刷新战力
//		refreshPowerAfterWar(atkPlayers, null);

		for (Player player : atkPlayers) {
			LocalRedis.getInstance().incrementAtkGundamTimes(WorldGundamService.getInstance().getGundamRefreshUuid(), player.getId());
		}
		
		if (point != null && point.getRemainBlood() > 0) {
			WorldPointService.getInstance().getWorldScene().update(point.getAoiObjId());
		}
	}
	
	/**
	 * 集结打怪行军返回
	 */
	default void gundamMarchReturn() {
		// 体力返还
		this.getPlayer().dealMsg(MsgId.RETURN_VIT, new MarchVitReturnBackMsgInvoker(this.getPlayer(), this));
		// 队长行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());

		// 队员行军返回
		Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			// 体力返还
			massJoinMarch.getPlayer().dealMsg(MsgId.RETURN_VIT, new MarchVitReturnBackMsgInvoker(massJoinMarch.getPlayer(), this));
			// 行军返回
			WorldMarchService.getInstance().onMarchReturn(massJoinMarch, massJoinMarch.getMarchEntity().getArmys(), getMarchEntity().getTerminalId());
		}
	}

	/**
	 * 发邮件：目标野怪消失
	 */
	default void sendPointErrorMail(Player leader) {
		// 进攻方玩儿家
		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(leader);

		// 填充参与集结信息
		Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			atkPlayers.add(GlobalData.getInstance().makesurePlayer(massJoinMarch.getPlayerId()));
		}
		
		for (Player player : atkPlayers) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.GUNDAM_DISAPPEAR)
					.build());
		}
	}
	
	/**
	 * 战斗结果处理(奖励、邮件处理)
	 */
	default void doBattleResult(Player leader, WorldPoint point, WorldGundamCfg gundamCfg, List<Player> atkPlayers, BattleOutcome battleOutcome) {
		// 总击杀怪物数量(血量)
		int totalKillCount = getTotalKillCount(battleOutcome);

		// 怪物剩余血量
		int remainBlood = calcAfterBlood(point, totalKillCount);
		
		// 设置怪物剩余血量
		point.setRemainBlood(remainBlood > 0 ? remainBlood : 0);

		// 战斗胜利 发击杀奖励
		if (remainBlood <= 0) {
			// 发邮件：击杀奖励邮件
			sendKillAwardMail(point, gundamCfg, atkPlayers, battleOutcome, remainBlood, totalKillCount);
		} else {
			// 发邮件：伤害奖励邮件
			sendAtkAward(point, gundamCfg, atkPlayers, battleOutcome, remainBlood, totalKillCount);
			
		}
	}
	
	/**
	 * 获取击杀总数量
	 */
	default int getTotalKillCount(BattleOutcome battleOutcome) {
		int totalKillCount = 0;
		String guildId = null;
		Map<String, List<ArmyInfo>> aftArmyMapAtk = battleOutcome.getAftArmyMapAtk();
		for(Entry<String, List<ArmyInfo>> entry : aftArmyMapAtk.entrySet()){
			String playerId = entry.getKey();
			guildId = GuildService.getInstance().getPlayerGuildId(playerId);
			List<ArmyInfo> armyInfos = entry.getValue();
			int selfTotalCnt = 0;
			for (ArmyInfo armyInfo : armyInfos) {
				selfTotalCnt  += armyInfo.getKillCount();
			}
			// 活动时间-攻击机甲BOSS
			ActivityManager.getInstance().postEvent(new MachineAwakeAttackEvent(playerId, guildId, selfTotalCnt));
			totalKillCount += selfTotalCnt;
		}
		
		return totalKillCount;
	}

	/**
	 * 计算怪物剩余血量(部队)
	 */
	default int calcAfterBlood(WorldPoint point, int totalKillCount) {
		// 伤害上限
		int killCountLimit = WorldMarchConstProperty.getInstance().getGundamOnceKillLimit();
		if (this.isMassMarch()) {
			killCountLimit = WorldMarchConstProperty.getInstance().getMassGundamOnceKillLimit();
		}
		
		totalKillCount = Math.min(totalKillCount, killCountLimit);
		
		// 攻打前怪物剩余血量
		int beforeBlood = point.getRemainBlood();
		// 攻击后怪物剩余血量
		int afterBlood = (beforeBlood >= totalKillCount) ? (beforeBlood - totalKillCount) : 0;
		return afterBlood;
	}
	
	/**
	 * 获取伤害比率
	 */
	default int getKillCount(BattleOutcome battleOutcome, Player player, int totalCount) {
		// 单人击杀玩家数量
		int playerKillCount = 0;
		List<ArmyInfo> playerArmyInfos = battleOutcome.getAftArmyMapAtk().get(player.getId());
		for (ArmyInfo playerArmyInfo : playerArmyInfos) {
			playerKillCount += playerArmyInfo.getKillCount();
		}
		
		// 伤害上限
		int killCountLimit = WorldMarchConstProperty.getInstance().getGundamOnceKillLimit();
		if (this.isMassMarch()) {
			killCountLimit = WorldMarchConstProperty.getInstance().getMassGundamOnceKillLimit();
		}
		
		playerKillCount = Math.min(playerKillCount, killCountLimit);
		playerKillCount = Math.max(playerKillCount, 1); // 总有傻子出一个兵 还来问为什么伤害为0
		// 伤害比率
		return playerKillCount;
	}
	
	/**
	 * 获取受伤部队数量
	 */
	default int getWoundCount(BattleOutcome battleOutcome, Player player) {
		int woundCount = 0;
		List<ArmyInfo> playerArmyInfos = battleOutcome.getAftArmyMapAtk().get(player.getId());
		for (ArmyInfo playerArmyInfo : playerArmyInfos) {
			woundCount += playerArmyInfo.getWoundedCount();
		}
		return woundCount;
	}

	
	/**
	 * 发邮件：战斗胜利邮件
	 */
	default void sendAtkAward(WorldPoint point, WorldGundamCfg gundamCfg, List<Player> atkPlayers, BattleOutcome battleOutcome, int remainBlood, int totalKillCount) {
		// 获取怪物最大血量
		int totalEnemyBlood = WorldGundamService.getInstance().getGundamInitBlood(gundamCfg.getId());
		
		for (Player player : atkPlayers) {
			// 获取伤害比率
			float killCount = getKillCount(battleOutcome, player, totalEnemyBlood);
			// 获取受伤部队数量
			int woundCount = getWoundCount(battleOutcome, player);
			// 发邮件:伤害奖励
			MonsterMail.Builder monsterMailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, Collections.emptyList(), gundamCfg.getId(), point, null, null, remainBlood, killCount, woundCount);
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.ATK_GUNDAM_WIN)
					.addContents(monsterMailBuilder)
					.build());
		}
	}
	
	/**
	 * 发邮件：击杀奖励邮件
	 */
	default void sendKillAwardMail(WorldPoint point, WorldGundamCfg gundamCfg, List<Player> atkPlayers, BattleOutcome battleOutcome, int remainBlood, int totalKillCount) {
		// 击杀奖励
		AwardItems killAward = AwardItems.valueOf();
		killAward.addAwards(gundamCfg.getKillAwards());
		
		// 获取怪物最大血量
		int totalEnemyBlood = WorldGundamService.getInstance().getGundamInitBlood(gundamCfg.getId());
		
		for (Player player : atkPlayers) {
			// 获取伤害比率
			float killCount = getKillCount(battleOutcome, player, totalEnemyBlood);
			// 获取受伤部队数量
			int woundCount = getWoundCount(battleOutcome, player);
			// 发邮件
			MonsterMail.Builder mailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, Collections.emptyList(), gundamCfg.getId(), point, killAward.getAwardItems(), null, remainBlood, killCount, woundCount);
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.KILL_GUNDAM)
					.addContents(mailBuilder)
					.setRewards(killAward.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addTips(gundamCfg.getId())
					.build());
		}
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
		builder.setPointType(WorldPointType.GUNDAM);
		builder.setX(pos[0]);
		builder.setY(pos[1]);

		int targetId = Integer.parseInt(this.getMarchEntity().getTargetId());
		builder.setMonsterId(targetId);
		return builder;
	}
	
	@Override
	default boolean needShowInGuildWar() {
		return this.isMassMarch();
	}
}
