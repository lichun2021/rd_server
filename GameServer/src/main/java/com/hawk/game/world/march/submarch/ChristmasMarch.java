package com.hawk.game.world.march.submarch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.ChristmasWarAttackEvent;
import com.hawk.activity.event.impl.MachineAwakeTwoEvent;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst.BattleType;
import com.hawk.game.config.WorldChristmasWarBossCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
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
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.TimeLimitStoreTriggerType;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldChristmasWarService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;
import com.hawk.log.Source;

public interface ChristmasMarch extends BasedMarch {

	@Override
	default void onMarchReach(Player leader) {
		// 行军
		WorldMarch leaderMarch = getMarchEntity();
		// 目标点
		int terminalId = leaderMarch.getTerminalId();
		// 目标id
		int bossId = Integer.valueOf(leaderMarch.getTargetId());
		// 点和怪信息
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(terminalId);

		// 点为空
		if (point == null) {
			christmasMarchReturn();
			sendPointErrorMail(leader);
			WorldMarchService.logger.info("attackChristmasMarch reach, point null, terminalId:{}", terminalId);
			
			return;
		}

		// 非野怪点
		if (point.getPointType() != WorldPointType.CHRISTMAS_BOSS_VALUE) {
			christmasMarchReturn();
			sendPointErrorMail(leader);
			WorldMarchService.logger.error("attackChristmasMarch reach, point not christmas, terminalId:{}", terminalId);
			
			return;
		}

		// 点改变
		if (point.getMonsterId() != bossId) {
			christmasMarchReturn();
			sendPointErrorMail(leader);
			WorldMarchService.logger.error("attackChristmasMarch reach, point has changed, terminalId:{}", terminalId);
			
			return;
		}

		// 圣诞boss
		WorldChristmasWarBossCfg bossCfg = HawkConfigManager.getInstance().getConfigByKey(WorldChristmasWarBossCfg.class, bossId);
		if (bossCfg == null) {
			christmasMarchReturn();
			sendPointErrorMail(leader);
			WorldMarchService.logger.error("attackChristmasMarch reach, monsterCfg null, christmasId:{}", bossId);
			
			return;
		}

		// boss血量.
		int beforeBlood = point.getRemainBlood();

		/**********************    战斗数据组装及战斗***************************/
		// 进攻方玩家
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
		PveBattleIncome battleIncome = BattleService.getInstance().initGundamBattleData(BattleType.ATTACK_GUNDAM_PVE,
				point.getId(), bossCfg.getId(), bossCfg.getArmyList(), atkMarchs);
		
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		/**********************战斗数据组装及战斗***************************/
		
		// 战斗结果处理
		
		doBattleResult(leader, point, bossCfg, atkPlayers, battleOutcome);
		// 行军返回
		WorldMarchService.getInstance().onMarchReturn(this, battleOutcome.getAftArmyMapAtk().get(this.getMarchEntity().getPlayerId()), 0);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			WorldMarchService.getInstance().onMarchReturn(massJoinMarch, battleOutcome.getAftArmyMapAtk().get(massJoinMarch.getMarchEntity().getPlayerId()), 0);
		}
		
		// 战斗胜利，移除点
		if (point.getRemainBlood() <= 0) {
			if (this.isMassMarch()) {
				String guildTag = GuildService.getInstance().getGuildTag(this.getPlayer().getGuildId());
				Const.NoticeCfgId noticeId = Const.NoticeCfgId.WORLD_CHRISTMAS_KILLED_GUILD;
				ChatParames.Builder chatBuilder = ChatParames.newBuilder();
				chatBuilder.setChatType(Const.ChatType.SPECIAL_BROADCAST);
				chatBuilder.setKey(noticeId);
				chatBuilder.addParms(guildTag);
				chatBuilder.addParms(this.getPlayer().getName());
				chatBuilder.addParms(point.getX());
				chatBuilder.addParms(point.getY());
				ChatService.getInstance().addWorldBroadcastMsg(chatBuilder.build());
			} else {
				Const.NoticeCfgId noticeId = Const.NoticeCfgId.WORLD_CHRISTMAS_KILLED_PERSONAL;
				ChatParames.Builder chatBuilder = ChatParames.newBuilder();
				chatBuilder.setChatType(Const.ChatType.SPECIAL_BROADCAST);
				chatBuilder.setKey(noticeId);				
				chatBuilder.addParms(this.getPlayer().getName());
				chatBuilder.addParms(point.getX());
				chatBuilder.addParms(point.getY());
				ChatService.getInstance().addWorldBroadcastMsg(chatBuilder.build());
			}
			
			WorldChristmasWarService.getInstance().onBossKill(point);
			
		}

		
		for (Player player : atkPlayers) {
			BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.CHRISTMAS_MARCH,
					Params.valueOf("pointId", point.getId()),
					Params.valueOf("x", point.getX()),
					Params.valueOf("y", point.getY()),
					Params.valueOf("christmasId", bossId),
					Params.valueOf("isKill", point.getRemainBlood() <= 0),
					Params.valueOf("isLeader", player.getId().equals(leader.getId())),
					Params.valueOf("isMass", this.isMassMarch()),
					Params.valueOf("beforeBlood", beforeBlood),
					Params.valueOf("afterBlood", point.getRemainBlood()));
		}

		for (Player player : atkPlayers) {
			player.incrementAtkNianTimes(WorldChristmasWarService.getInstance().getRefreshUuid());
			HawkApp.getInstance().postMsg(player, new TimeLimitStoreTriggerMsg(TimeLimitStoreTriggerType.ATTACK_MONSTER, 1));
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
				task.setTypeName("ChristmasMarch");
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
	default void christmasMarchReturn() {
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
			
		MailId mailId = MailId.CHRISTMAS_MISS;		
		
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
	default void doBattleResult(Player leader, WorldPoint point, WorldChristmasWarBossCfg bossCfg, List<Player> atkPlayers, BattleOutcome battleOutcome) {
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
			sendKillAwardMail(point, bossCfg, atkPlayers, battleOutcome, afterBlood, totalKillCount);
			
			// 发送战斗结果 集结野怪只有胜利
			WorldMarchService.getInstance().sendBattleResultInfo(this, true, WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk()), new ArrayList<ArmyInfo>(), point.getRemainBlood() <= 0, false);
		} else {
			
			int totalBlood = WorldChristmasWarService.getInstance().getInitBlood();
			int hpNumber = bossCfg.getHpNumber();
			int oneHpBlood = totalBlood / hpNumber;
			
			int beforeHpNumber = Math.min(((beforeBlood - 1) / oneHpBlood + 1), hpNumber);
			int afterHpNumber = Math.min(((afterBlood - 1) / oneHpBlood + 1), hpNumber);
			if (beforeHpNumber != afterHpNumber) {
				if (this.isMassMarch()) {
					String guildTag = GuildService.getInstance().getGuildTag(this.getPlayer().getGuildId());
					Const.NoticeCfgId noticeId = Const.NoticeCfgId.WORLD_CHRISTMAS_GUILD_ONCE;
					ChatParames.Builder chatBuilder = ChatParames.newBuilder();
					chatBuilder.setChatType(Const.ChatType.SPECIAL_BROADCAST);
					chatBuilder.setKey(noticeId);
					chatBuilder.addParms(guildTag).addParms(this.getPlayer().getName()).addParms(point.getX()).addParms(point.getY());
					ChatService.getInstance().addWorldBroadcastMsg(chatBuilder.build());
				} else {
					Const.NoticeCfgId noticeId = Const.NoticeCfgId.WORLD_CHRISTMAS_PERSONAL_ONCE;
					ChatParames.Builder chatBuilder = ChatParames.newBuilder();
					chatBuilder.setChatType(Const.ChatType.SPECIAL_BROADCAST);
					chatBuilder.setKey(noticeId);
					chatBuilder.addParms(this.getPlayer().getName()).addParms(point.getX()).addParms(point.getY());
					ChatService.getInstance().addWorldBroadcastMsg(chatBuilder.build());
				}
				
				// 发邮件：致命一击奖励邮件
				sendOnceKillAwardMail(point, bossCfg, atkPlayers, battleOutcome, afterBlood, totalKillCount);
				
				// 发送战斗结果 集结野怪只有胜利
				WorldMarchService.getInstance().sendBattleResultInfo(this, true, WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk()), new ArrayList<ArmyInfo>(), point.getRemainBlood() <= 0, true);
				
			} else {
				// 发邮件：伤害奖励邮件
				sendAtkAward(point, bossCfg, atkPlayers, battleOutcome, afterBlood, totalKillCount);
				
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
			// 圣诞boss攻击.
			ActivityManager.getInstance().postEvent(new ChristmasWarAttackEvent(playerId, guildId, selfTotalCnt));
			totalKillCount += selfTotalCnt;
		}
		
		return totalKillCount;
	}

	/**
	 * 计算怪物剩余血量(部队)
	 */
	default int calcAfterBlood(WorldPoint point, int totalKillCount) {
		
		totalKillCount = this.getKillCount(totalKillCount);
		
		// 攻打前怪物剩余血量
		int beforeBlood = point.getRemainBlood();
		// 攻击后怪物剩余血量
		int afterBlood = (beforeBlood >= totalKillCount) ? (beforeBlood - totalKillCount) : 0;
		return afterBlood;
	}
	
	default int getKillCountMin() {
		WorldMarchConstProperty constProperty = WorldMarchConstProperty.getInstance();  
		// 伤害上限
		double killpercent = constProperty.getChristmasDeadlinessAtkMin();
		if (this.isMassMarch()) {
			killpercent = constProperty.getChristmasMassDeadlinessAtkMin();
		}
		
		int maxBlood = WorldChristmasWarService.getInstance().getInitBlood();
		int killCountMin = (int)Math.ceil(maxBlood * (killpercent / GsConst.RANDOM_MYRIABIT_BASE));
		
		return killCountMin;
	}
	
	default int getKillCountLimit() {
		WorldMarchConstProperty constProperty = WorldMarchConstProperty.getInstance();  
		// 伤害上限
		double killpercent = constProperty.getChristmasDeadlinessAtkLimit();
		if (this.isMassMarch()) {
			killpercent = constProperty.getChristmasMassDeadlinessAtkLimit();
		}
		
		int maxBlood = WorldChristmasWarService.getInstance().getInitBlood();
		int killCountLimit = (int)Math.ceil(maxBlood * (killpercent / GsConst.RANDOM_MYRIABIT_BASE));
		return killCountLimit;
	}
	
	/**
	 * 获取伤害比率
	 */
	default int getKillCount(BattleOutcome battleOutcome, Player player, int totalCount, int christmasId) {
		// 单人击杀玩家数量
		int playerKillCount = 0;
		List<ArmyInfo> playerArmyInfos = battleOutcome.getAftArmyMapAtk().get(player.getId());
		for (ArmyInfo playerArmyInfo : playerArmyInfos) {
			playerKillCount += playerArmyInfo.getKillCount();
		}
		
		playerKillCount = this.getKillCount(playerKillCount);
		
		// 伤害比率
		return playerKillCount;
	}
	
	default int getKillCount(int playerKillCount) {
		int killCount = playerKillCount;
		int minKillCount = this.getKillCountMin();
		if (minKillCount > playerKillCount) {
			killCount = minKillCount;
		}
		
		int limitKillCount = this.getKillCountLimit();
		if (limitKillCount < killCount) {
			killCount = limitKillCount;
		}
		
		return killCount;
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
	default void sendAtkAward(WorldPoint point, WorldChristmasWarBossCfg bossCfg, List<Player> atkPlayers, BattleOutcome battleOutcome, int remainBlood, int totalKillCount) {
		// 获取怪物最大血量
		int totalEnemyBlood = WorldChristmasWarService.getInstance().getInitBlood();
		
		AwardItems atkAward = AwardItems.valueOf();
		atkAward.addAwards(bossCfg.getAtkAwards());
		
		MailId mailId = MailId.CHRISTMAS_ATK;
		
		for (Player player : atkPlayers) {
			// 获取伤害比率
			float killCount = getKillCount(battleOutcome, player, totalEnemyBlood, bossCfg.getId());
			
			// 获取受伤部队数量
			int woundCount = getWoundCount(battleOutcome, player);
			// 发邮件:伤害奖励
			MonsterMail.Builder monsterMailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, Collections.emptyList(), bossCfg.getId(), point, atkAward.getAwardItems(), null, remainBlood, killCount, woundCount);
			monsterMailBuilder.setIsAtkMax(killCount >= getKillCountLimit());
			monsterMailBuilder.setMaxBlood(WorldChristmasWarService.getInstance().getInitBlood());
			
			
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
	default void sendOnceKillAwardMail(WorldPoint point, WorldChristmasWarBossCfg bossCfg, List<Player> atkPlayers, BattleOutcome battleOutcome, int remainBlood, int totalKillCount) {
		
		// 获取怪物最大血量
		int totalEnemyBlood = WorldChristmasWarService.getInstance().getInitBlood();
		
		for (Player player : atkPlayers) {

			// 致命一击奖励
			AwardItems killAward = AwardItems.valueOf();
			killAward.addAwards(bossCfg.getDeadlyAwards());

			// 伤害奖励
			AwardItems atkAward = AwardItems.valueOf();
			atkAward.addAwards(bossCfg.getAtkAwards());

			//集结奖励
			AwardItems deadlyMassAward = AwardItems.valueOf();
			deadlyMassAward.addAwards(bossCfg.getDeadlyMassAwards());

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

			AwardItems sendAward = AwardItems.valueOf();
			sendAward.addItemInfos(new ArrayList<>(killAward.getAwardItems()));
			sendAward.addItemInfos(new ArrayList<>(atkAward.getAwardItems()));

			// 获取伤害比率
			float killCount = getKillCount(battleOutcome, player, totalEnemyBlood, bossCfg.getId());
			
			// 获取受伤部队数量
			int woundCount = getWoundCount(battleOutcome, player);
			// 发邮件
			MonsterMail.Builder mailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, Collections.emptyList(), bossCfg.getId(), point, atkAward.getAwardItems(), null, remainBlood, killCount, woundCount);
			mailBuilder.setIsAtkMax(killCount >= getKillCountLimit());
			mailBuilder.setMaxBlood(totalEnemyBlood);
			
			for (ItemInfo awardItem : killAward.getAwardItems()) {
				mailBuilder.addKillReward(awardItem.toRewardItem());
			}
			
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.CHRISTMAS_PERSONAL_DEADLINESS)
					.addContents(mailBuilder)
					.setRewards(sendAward.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addTips(bossCfg.getId())
					.build());
			
			if (atkPlayers.size() > 1) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(player.getId())
						.setMailId(MailId.CHRISTMAS_MASS_DEADLINESS)
						.setRewards(deadlyMassAward.getAwardItems())
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.build());
			}
			
			ActivityManager.getInstance().postEvent(new MachineAwakeTwoEvent(player.getId(), atkPlayers.size() > 1, true, false));
			//todo
			//LogUtil.logOnceKillGundam(player, point.getX(), point.getY());
		}
	}
	
	/**
	 * 发邮件：击杀奖励邮件
	 */
	default void sendKillAwardMail(WorldPoint point, WorldChristmasWarBossCfg bossCfg, List<Player> atkPlayers, BattleOutcome battleOutcome, int remainBlood, int totalKillCount) {

		// 获取怪物最大血量
		int totalEnemyBlood = WorldChristmasWarService.getInstance().getInitBlood();

		for (Player player : atkPlayers) {

			// 击杀奖励
			AwardItems killAward = AwardItems.valueOf();
			killAward.addAwards(bossCfg.getKillAwards());

			// 伤害奖励
			AwardItems atkAward = AwardItems.valueOf();
			atkAward.addAwards(bossCfg.getAtkAwards());

			// 集结奖励
			AwardItems killMassAward = AwardItems.valueOf();
			killMassAward.addAwards(bossCfg.getKillMassAwards());

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

			AwardItems sendAward = AwardItems.valueOf();
			sendAward.addItemInfos(new ArrayList<>(killAward.getAwardItems()));
			sendAward.addItemInfos(new ArrayList<>(atkAward.getAwardItems()));
			// 获取伤害比率
			float killCount = getKillCount(battleOutcome, player, totalEnemyBlood, bossCfg.getId());
			
			// 获取受伤部队数量
			int woundCount = getWoundCount(battleOutcome, player);
			// 发邮件
			MonsterMail.Builder mailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, Collections.emptyList(), bossCfg.getId(), point, atkAward.getAwardItems(), null,
					remainBlood, killCount, woundCount);
			mailBuilder.setIsAtkMax(killCount >= getKillCountLimit());
			mailBuilder.setMaxBlood(totalEnemyBlood);
			
			for (ItemInfo awardItem : killAward.getAwardItems()) {
				mailBuilder.addKillReward(awardItem.toRewardItem());
			}

			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.CHRISTMAS_PERSONAL_KILL)
					.addContents(mailBuilder)
					.setRewards(sendAward.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addTips(bossCfg.getId())
					.build());
			
			if (atkPlayers.size() > 1) {

				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(player.getId())
						.setMailId(MailId.CHRISTMAS_MASS_KILL)
						.setRewards(killMassAward.getAwardItems())
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.build());
			}
			
			ActivityManager.getInstance().postEvent(new MachineAwakeTwoEvent(player.getId(), atkPlayers.size() > 1, false, true));
			//todo
			//LogUtil.logKillGundam(player, point.getX(), point.getY());
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
		builder.setPointType(WorldPointType.CHRISTMAS_BOSS);
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
