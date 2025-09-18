package com.hawk.game.world.march.submarch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.hawk.game.invoker.guildBackActivity.GuildBackDropInvoker;
import com.hawk.game.service.ActivityService;
import com.hawk.gamelib.GameConst;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.MachineAwakeTwoAttackEvent;
import com.hawk.activity.event.impl.MachineAwakeTwoEvent;
import com.hawk.activity.type.impl.machineAwakeTwo.cfg.MachineAwakeTwoActivityKVCfg;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst.BattleType;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.config.WorldNianCfg;
import com.hawk.game.entity.StatisticsEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.msg.TimeLimitStoreTriggerMsg;
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
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.StatisticDataType;
import com.hawk.game.util.GsConst.TimeLimitStoreTriggerType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldNianService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;
import com.hawk.log.Source;

public interface NianMarch extends BasedMarch {
	
	default BattleOutcome dobattle() {
		Player leader = getPlayer();
		// 行军
		WorldMarch leaderMarch = getMarchEntity();
		// 目标点
		int terminalId = leaderMarch.getTerminalId();
		// 目标高达
		int nianId = Integer.valueOf(leaderMarch.getTargetId());
		// 点和怪信息
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(terminalId);

		// 年兽配置
		WorldNianCfg nianCfg = HawkConfigManager.getInstance().getConfigByKey(WorldNianCfg.class, nianId);

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

		// 战斗数据输入
		PveBattleIncome battleIncome = BattleService.getInstance().initGundamBattleData(BattleType.ATTACK_GUNDAM_PVE, point.getId(), nianCfg.getId(), nianCfg.getArmyList(),
				atkMarchs);

		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		return battleOutcome;
	}
	
	@Override
	default void onMarchReach(Player leader) {
		// 行军
		WorldMarch leaderMarch = getMarchEntity();
		// 目标点
		int terminalId = leaderMarch.getTerminalId();
		// 目标高达
		int nianId = Integer.valueOf(leaderMarch.getTargetId());
		// 点和怪信息
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(terminalId);

		// 点为空
		if (point == null) {
			nianMarchReturn();
			sendPointErrorMail(leader);
			WorldMarchService.logger.info("attackNianMarch reach, point null, terminalId:{}", terminalId);
			return;
		}

		// 非野怪点
		if (point.getPointType() != WorldPointType.NIAN_VALUE) {
			nianMarchReturn();
			sendPointErrorMail(leader);
			WorldMarchService.logger.error("attackNianMarch reach, point not nian, terminalId:{}", terminalId);
			return;
		}

		// 点改变
		if (point.getMonsterId() != nianId) {
			nianMarchReturn();
			sendPointErrorMail(leader);
			WorldMarchService.logger.error("attackNianMarch reach, point has changed, terminalId:{}", terminalId);
			return;
		}

		// 年兽配置
		WorldNianCfg nianCfg = HawkConfigManager.getInstance().getConfigByKey(WorldNianCfg.class, nianId);
		if (nianCfg == null) {
			nianMarchReturn();
			sendPointErrorMail(leader);
			WorldMarchService.logger.error("attackNianMarch reach, monsterCfg null, nianId:{}", nianId);
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
		
		
		/**********************战斗数据组装及战斗***************************/
		
		BattleOutcome battleOutcome = dobattle();
		
		// 战斗结果处理
		doBattleResult(leader, point, nianCfg, atkPlayers, battleOutcome);
		
		// 行军返回
		WorldMarchService.getInstance().onMarchReturn(this, battleOutcome.getAftArmyMapAtk().get(this.getMarchEntity().getPlayerId()), 0);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			WorldMarchService.getInstance().onMarchReturn(massJoinMarch, battleOutcome.getAftArmyMapAtk().get(massJoinMarch.getMarchEntity().getPlayerId()), 0);
		}
		
		// 战斗胜利，移除点
		if (point.getRemainBlood() <= 0) {
			if (this.isMassMarch()) {
				String guildTag = GuildService.getInstance().getGuildTag(this.getPlayer().getGuildId());
				Const.NoticeCfgId noticeId = isGhost() ? Const.NoticeCfgId.GHOST_3 : Const.NoticeCfgId.WORLD_NIAN_GUILD_KILLED;
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, noticeId, null, guildTag, this.getPlayer().getName(), point.getX(), point.getY());
			} else {
				Const.NoticeCfgId noticeId = isGhost() ? Const.NoticeCfgId.GHOST_2 : Const.NoticeCfgId.WORLD_NIAN_PLAYER_KILLED;
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, noticeId, null, this.getPlayer().getName(), point.getX(), point.getY());
			}
			
			WorldNianService.getInstance().notifyNianKilled(point.getId());

			// 生成年兽宝箱
			int nianType = WorldNianService.getInstance().getNianType();
			if (nianType == GsConst.WORLD_NIAN_GHODT) {
				WorldPointService.getInstance().genGundamBox(point.getId(), point.getMonsterId(), true);
			}
		}

		boolean mass = this.isMassMarch();
		for (Player player : atkPlayers) {
			if (mass) {
				StatisticsEntity statisticsEntity = player.getData().getStatisticsEntity();
				statisticsEntity.addCommonStatisData(StatisticDataType.GROUP_TOTAL_TODAY, 1);
			}
			BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.NIAN_MARCH,
					Params.valueOf("pointId", point.getId()),
					Params.valueOf("x", point.getX()),
					Params.valueOf("y", point.getY()),
					Params.valueOf("nianId", nianId),
					Params.valueOf("isKill", point.getRemainBlood() <= 0),
					Params.valueOf("isLeader", player.getId().equals(leader.getId())),
					Params.valueOf("isMass", mass),
					Params.valueOf("beforeBlood", beforeBlood),
					Params.valueOf("afterBlood", point.getRemainBlood()));
		}

		// 刷新战力
//		refreshPowerAfterWar(atkPlayers, null);
		List<String> atkPlayerIds = new ArrayList<>();
		for (Player player : atkPlayers) {
			player.incrementAtkNianTimes(WorldNianService.getInstance().getNianRefreshUuid());
			HawkApp.getInstance().postMsg(player, new TimeLimitStoreTriggerMsg(TimeLimitStoreTriggerType.ATTACK_MONSTER, 1));
			atkPlayerIds.add(player.getId());
		}
		try {
			ActivityService.getInstance().dealMsg(GameConst.MsgId.ON_GUILD_BACK_DROP, new GuildBackDropInvoker(atkPlayerIds));
		}catch (Exception e){
			HawkException.catchException(e);
		}
		
		if (point != null && point.getRemainBlood() > 0) {
			
			HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
			if (threadPool != null) {
				HawkTask task = new HawkTask() {
					@Override
					public Object run() {
						WorldPointService.getInstance().getWorldScene().update(point.getAoiObjId());
						return null;
					}
				};
				task.setTypeName("NianMarch");
				int threadIndex = Math.abs(point.getAoiObjId() % threadPool.getThreadNum());
				threadPool.addTask(task, threadIndex, false);
			} else {
				WorldPointService.getInstance().getWorldScene().update(point.getAoiObjId());
			}
			
		}
	}
	
	/**
	 * 集结打怪行军返回
	 */
	default void nianMarchReturn() {
		// 队长行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());

		// 队员行军返回
		Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
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
		
		MailId mailId = MailId.NIAN_DISAPPEAR;
		if (isGhost()) {
			mailId = MailId.GHOST_NIAN_1;
		}
		
		for (Player player : atkPlayers) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(mailId)
					.build());
		}
	}
	
	/**
	 * 战斗结果处理(奖励、邮件处理)
	 */
	default void doBattleResult(Player leader, WorldPoint point, WorldNianCfg nianCfg, List<Player> atkPlayers, BattleOutcome battleOutcome) {
		// 结算前血量
		int beforeBlood = point.getRemainBlood();
		
		// 总击杀怪物数量(血量)
		int totalKillCount = getTotalKillCount(battleOutcome);

		// 计算后血量
		int afterBlood = calcAfterBlood(point, totalKillCount);
		
		// 设置怪物剩余血量
		point.setRemainBlood(afterBlood > 0 ? afterBlood : 0);

		// 战斗胜利 发击杀奖励
		if (afterBlood <= 0) {
			// 发邮件：击杀奖励邮件
			sendKillAwardMail(point, nianCfg, atkPlayers, battleOutcome, afterBlood, totalKillCount);
			
			// 发送战斗结果 集结野怪只有胜利
			WorldMarchService.getInstance().sendBattleResultInfo(this, true, WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk()), new ArrayList<ArmyInfo>(), point.getRemainBlood() <= 0, false);
		} else {
			
			int totalBlood = WorldNianService.getInstance().getNianInitBlood(nianCfg.getId());
			int hpNumber = nianCfg.getHpNumber();
			int oneHpBlood = totalBlood / hpNumber;
			
			int beforeHpNumber = Math.min(((beforeBlood - 1) / oneHpBlood + 1), hpNumber);
			int afterHpNumber = Math.min(((afterBlood - 1) / oneHpBlood + 1), hpNumber);
			if (beforeHpNumber != afterHpNumber) {
				if (this.isMassMarch()) {
					String guildTag = GuildService.getInstance().getGuildTag(this.getPlayer().getGuildId());
					Const.NoticeCfgId noticeId = isGhost() ? Const.NoticeCfgId.GHOST_5 : Const.NoticeCfgId.WORLD_NIAN_GUILD_ONCE;
					ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, noticeId, null, guildTag, this.getPlayer().getName(), point.getX(), point.getY());
				} else {
					Const.NoticeCfgId noticeId = isGhost() ? Const.NoticeCfgId.GHOST_4 : Const.NoticeCfgId.WORLD_NIAN_PLAYER_ONCE;
					ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, noticeId, null, this.getPlayer().getName(), point.getX(), point.getY());
				}
				
				// 发邮件：致命一击奖励邮件
				sendOnceKillAwardMail(point, nianCfg, atkPlayers, battleOutcome, afterBlood, totalKillCount);
				
				// 发送战斗结果 集结野怪只有胜利
				WorldMarchService.getInstance().sendBattleResultInfo(this, true, WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk()), new ArrayList<ArmyInfo>(), point.getRemainBlood() <= 0, true);
				
				// 生成年兽宝箱
				int nianType = WorldNianService.getInstance().getNianType();
				if (nianType == GsConst.WORLD_NIAN_GHODT) {
					WorldPointService.getInstance().genGundamBox(point.getId(), point.getMonsterId(), false);
				}
				
			} else {
				// 发邮件：伤害奖励邮件
				sendAtkAward(point, nianCfg, atkPlayers, battleOutcome, afterBlood, totalKillCount);
				
				// 发送战斗结果 集结野怪只有胜利
				WorldMarchService.getInstance().sendBattleResultInfo(this, true, WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk()), new ArrayList<ArmyInfo>(), point.getRemainBlood() <= 0, false);
			}
		}
	}
	
	/**
	 * 获取击杀总数量
	 */
	default int getTotalKillCount(BattleOutcome battleOutcome) {
		int totalKillCount = 0;
		Map<String, List<ArmyInfo>> aftArmyMapAtk = battleOutcome.getAftArmyMapAtk();
		for(Entry<String, List<ArmyInfo>> entry : aftArmyMapAtk.entrySet()){
			List<ArmyInfo> armyInfos = entry.getValue();
			int selfTotalCnt = 0;
			for (ArmyInfo armyInfo : armyInfos) {
				selfTotalCnt  += armyInfo.getKillCount();
			}
			
			totalKillCount += selfTotalCnt;
		}
		
		return totalKillCount;
	}

	/**
	 * 计算怪物剩余血量(部队)
	 */
	default int calcAfterBlood(WorldPoint point, int totalKillCount) {
		
		totalKillCount = Math.min(totalKillCount, getKillCountLimit(point.getMonsterId()));
		
		// 攻打前怪物剩余血量
		int beforeBlood = point.getRemainBlood();
		// 攻击后怪物剩余血量
		int afterBlood = (beforeBlood >= totalKillCount) ? (beforeBlood - totalKillCount) : 0;
		return afterBlood;
	}
	
	default int getKillCountLimit(int nianId) {
		// 伤害上限
		double killpercent = WorldMarchConstProperty.getInstance().getNianOnceKillLimit();
		if (this.isMassMarch()) {
			killpercent = WorldMarchConstProperty.getInstance().getMassNianOnceKillLimit();
		}
		
		int maxBlood = WorldNianService.getInstance().getNianInitBlood(nianId);
		int killCountLimit = (int)Math.ceil(maxBlood * (killpercent / GsConst.RANDOM_MYRIABIT_BASE));
		return killCountLimit;
	}
	
	/**
	 * 获取伤害比率
	 */
	default int getKillCount(BattleOutcome battleOutcome, Player player, int totalKillCount, int nianId) {
		// 单人击杀玩家数量
		int playerKillCount = 0;
		MachineAwakeTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MachineAwakeTwoActivityKVCfg.class);
		double sharePoint = cfg.getSharePoint()* GsConst.EFF_PER;
		List<ArmyInfo> playerArmyInfos = battleOutcome.getAftArmyMapAtk().get(player.getId());
		for (ArmyInfo playerArmyInfo : playerArmyInfos) {
			playerKillCount += playerArmyInfo.getKillCount();
		}
		
		// 个人伤害的80% + 总伤害20% 平摊给每个人
		playerKillCount = (int) (playerKillCount * (1 - sharePoint) + totalKillCount * sharePoint / battleOutcome.getBattleArmyMapAtk().size());
		
		playerKillCount = Math.min(playerKillCount, getKillCountLimit(nianId));
		// 活动时间-攻击年兽BOSS
		ActivityManager.getInstance().postEvent(new MachineAwakeTwoAttackEvent(player.getId(), player.getGuildId(), playerKillCount));
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
	default void sendAtkAward(WorldPoint point, WorldNianCfg nianCfg, List<Player> atkPlayers, BattleOutcome battleOutcome, int remainBlood, int totalKillCount) {
		
		AwardItems atkAward = AwardItems.valueOf();
		atkAward.addAwards(nianCfg.getAtkAwards());
		
		MailId mailId = MailId.ATK_NIAN;
		if (isGhost()) {
			mailId = MailId.GHOST_NIAN_2;
		}
		
		for (Player player : atkPlayers) {
			// 获取伤害比率
			float killCount = getKillCount(battleOutcome, player, totalKillCount, nianCfg.getId());
			
			// 获取受伤部队数量
			int woundCount = getWoundCount(battleOutcome, player);
			// 发邮件:伤害奖励
			MonsterMail.Builder monsterMailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, Collections.emptyList(), nianCfg.getId(), point, atkAward.getAwardItems(), null, remainBlood, killCount, woundCount);
			monsterMailBuilder.setIsAtkMax(killCount >= getKillCountLimit(nianCfg.getId()));
			monsterMailBuilder.setMaxBlood(WorldNianService.getInstance().getNianInitBlood(nianCfg.getId()));
			
			
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(mailId)
					.setRewards(atkAward.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addContents(monsterMailBuilder)
					.build());
			ActivityManager.getInstance().postEvent(new MachineAwakeTwoEvent(player.getId(), atkPlayers.size() > 1, false, false));
		}
	}
	
	/**
	 * 发邮件：致命一击奖励邮件
	 */
	default void sendOnceKillAwardMail(WorldPoint point, WorldNianCfg nianCfg, List<Player> atkPlayers, BattleOutcome battleOutcome, int remainBlood, int totalKillCount) {

		for (Player player : atkPlayers) {
			// 致命一击奖励
			AwardItems killAward = AwardItems.valueOf();
			killAward.addAwards(nianCfg.getDeadlyAwards());

			// 伤害奖励
			AwardItems atkAward = AwardItems.valueOf();
			atkAward.addAwards(nianCfg.getAtkAwards());

			// 集结奖励
			AwardItems deadlyMassAward = AwardItems.valueOf();
			deadlyMassAward.addAwards(nianCfg.getDeadlyMassAwards());
			
			if (!isGhost()){
				// 击杀奖励添加作用号效果
				int effVal643 = player.getEffect().getEffVal(Const.EffType.LIFE_TIME_CARD_643);
				if (effVal643 > 0) {
					deadlyMassAward.getAwardItems().forEach(item ->{
						item.setCount((long)(item.getCount() * (effVal643 * 0.0001 + 1)));
					});
					killAward.getAwardItems().forEach(item ->{
						item.setCount((long)(item.getCount() * (effVal643 * 0.0001 + 1)));
					});
				}
			}
			
			AwardItems sendAward = AwardItems.valueOf();
			sendAward.addItemInfos(new ArrayList<>(killAward.getAwardItems()));
			sendAward.addItemInfos(new ArrayList<>(atkAward.getAwardItems()));
			

			// 获取伤害比率
			float killCount = getKillCount(battleOutcome, player, totalKillCount, nianCfg.getId());
			
			// 获取受伤部队数量
			int woundCount = getWoundCount(battleOutcome, player);
			// 发邮件
			MonsterMail.Builder mailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, Collections.emptyList(), nianCfg.getId(), point, atkAward.getAwardItems(), null, remainBlood, killCount, woundCount);
			mailBuilder.setIsAtkMax(killCount >= getKillCountLimit(nianCfg.getId()));
			mailBuilder.setMaxBlood(WorldNianService.getInstance().getNianInitBlood(nianCfg.getId()));
			for (ItemInfo awardItem : killAward.getAwardItems()) {
				mailBuilder.addKillReward(awardItem.toRewardItem());
			}
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(isGhost() ? MailId.GHOST_NIAN_3 : MailId.ONCE_KILL_NIAN)
					.addContents(mailBuilder)
					.setRewards(sendAward.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addTips(nianCfg.getId())
					.build());
			
			if (atkPlayers.size() > 1) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(player.getId())
						.setMailId(isGhost() ? MailId.GHOST_NIAN_7 : MailId.NIAN_MASS_ONCE_KILL_AWARD)
						.setRewards(deadlyMassAward.getAwardItems())
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.build());
			}
			ActivityManager.getInstance().postEvent(new MachineAwakeTwoEvent(player.getId(), atkPlayers.size() > 1, true, false));
			LogUtil.logOnceKillGundam(player, point.getX(), point.getY());
		}
	}
	
	/**
	 * 发邮件：击杀奖励邮件
	 */
	default void sendKillAwardMail(WorldPoint point, WorldNianCfg nianCfg, List<Player> atkPlayers, BattleOutcome battleOutcome, int remainBlood, int totalKillCount) {

		for (Player player : atkPlayers) {
			// 击杀奖励
			AwardItems killAward = AwardItems.valueOf();
			killAward.addAwards(nianCfg.getKillAwards());

			// 伤害奖励
			AwardItems atkAward = AwardItems.valueOf();
			atkAward.addAwards(nianCfg.getAtkAwards());

			//集结奖励
			AwardItems killMassAward = AwardItems.valueOf();
			killMassAward.addAwards(nianCfg.getKillMassAwards());


			if (!isGhost()){
				// 击杀奖励添加作用号效果
				int effVal643 = player.getEffect().getEffVal(Const.EffType.LIFE_TIME_CARD_643);
				if (effVal643 > 0) {
					killMassAward.getAwardItems().forEach(item ->{
						item.setCount((long)(item.getCount() * (effVal643 * 0.0001 + 1)));
					});
					killAward.getAwardItems().forEach(item ->{
						item.setCount((long)(item.getCount() * (effVal643 * 0.0001 + 1)));
					});
				}
			}

			AwardItems sendAward = AwardItems.valueOf();
			sendAward.addItemInfos(new ArrayList<>(killAward.getAwardItems()));
			sendAward.addItemInfos(new ArrayList<>(atkAward.getAwardItems()));
			// 获取伤害比率
			float killCount = getKillCount(battleOutcome, player, totalKillCount, nianCfg.getId());
			
			// 获取受伤部队数量
			int woundCount = getWoundCount(battleOutcome, player);
			// 发邮件
			MonsterMail.Builder mailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, Collections.emptyList(), nianCfg.getId(), point, atkAward.getAwardItems(), null,
					remainBlood, killCount, woundCount);
			mailBuilder.setIsAtkMax(killCount >= getKillCountLimit(nianCfg.getId()));
			mailBuilder.setMaxBlood(WorldNianService.getInstance().getNianInitBlood(nianCfg.getId()));
			
			for (ItemInfo awardItem : killAward.getAwardItems()) {
				mailBuilder.addKillReward(awardItem.toRewardItem());
			}

			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(isGhost() ? MailId.GHOST_NIAN_4 : MailId.KILL_NIAN)
					.addContents(mailBuilder)
					.setRewards(sendAward.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addTips(nianCfg.getId())
					.build());
			
			if (atkPlayers.size() > 1) {

				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(player.getId())
						.setMailId(isGhost() ? MailId.GHOST_NIAN_8 : MailId.NIAN_MASS_KILL_AWARD)
						.setRewards(killMassAward.getAwardItems())
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.build());
			}
			ActivityManager.getInstance().postEvent(new MachineAwakeTwoEvent(player.getId(), atkPlayers.size() > 1, false, true));
			
			LogUtil.logKillGundam(player, point.getX(), point.getY());
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
		builder.setPointType(WorldPointType.NIAN);
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
	
	default boolean isGhost() {
		int nianType = WorldNianService.getInstance().getNianType();
		return nianType == GsConst.WORLD_NIAN_GHODT;		
	}
}
