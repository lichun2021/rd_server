package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hawk.game.invoker.MarchVitReturnBackMsgInvoker;
import com.hawk.game.invoker.guildBackActivity.GuildBackDropInvoker;
import com.hawk.game.service.ActivityService;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.GameConst.MsgId;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.google.common.collect.ImmutableMap;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.event.impl.MonsterBossAttackEvent;
import com.hawk.activity.event.impl.VitCostEvent;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.entity.StatisticsEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.guild.guildrank.GuildRankMgr;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.msg.PlayerVitCostMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.Mail.MonsterMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.guildtask.event.KillMonsterTaskEvent;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.AlgorithmPoint;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.util.GsConst.StatisticDataType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.MassMarch;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.service.WorldMonsterService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 集结攻打野怪行军
 * 
 * @author golden
 *
 */
public class MassMonsterMarch extends PassiveMarch implements MassMarch {
	
	public MassMonsterMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.MONSTER_MASS;
	}

	@Override
	public WorldMarchType getJoinMassType() {
		return WorldMarchType.MONSTER_MASS_JOIN;
	}

	/**
	 * 集结等待类型行军处理
	 */
	@Override
	public void waitingStatusMarchProcess() {
		long currentTime = HawkTime.getMillisecond();
		// 行军
		WorldMarch leaderMarch = getMarchEntity();

		// 不是集结状态
		if (leaderMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			return;
		}
		// 未到出发时间
		if (leaderMarch.getStartTime() > currentTime) {
			return;
		}

		// 没有加入集结的行军到达，集结解散, 未到达的行军返回
		Set<IWorldMarch> reachJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
		if (reachJoinMarchs.size() <= 0) {
			// 体力返还
			WorldMarchService.getInstance().onMonsterRelatedMarchAction(this);
			// 行军返还
			WorldMarchService.getInstance().onMarchReturnImmediately(this, leaderMarch.getArmys());
			// 发邮件：没有集结加入的行军 集结已解散
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(leaderMarch.getPlayerId())
					.setMailId(MailId.MASS_MONSTER_HAS_NO_JOIN_MARCH)
					.build());

			// 未到达的行军返回
			Set<IWorldMarch> joinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, false);
			for (IWorldMarch joinMarch : joinMarchs) {

				double backX = getMarchEntity().getOrigionX();
				double backY = getMarchEntity().getOrigionY();
				AlgorithmPoint currPoint = WorldUtil.getMarchCurrentPosition(joinMarch.getMarchEntity());
				if (currPoint != null) {
					backX = currPoint.getX();
					backY = currPoint.getY();
				}

				// 体力返还
				WorldMarchService.getInstance().onMonsterRelatedMarchAction(joinMarch);
				// 行军返回
				WorldMarchService.getInstance().onMarchReturn(joinMarch, currentTime, getMarchEntity().getAwardItems(), joinMarch.getMarchEntity().getArmys(), backX, backY);
				// 发邮件：队长行军已经出发
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(joinMarch.getPlayerId())
						.setMailId(MailId.MASS_MONSTER_NOT_REACH)
						.build());
			}
			WorldMarchService.logger.info("mass monster march failed, have no mass join march, march:{}", leaderMarch);
			return;
		}

		// 队长行军处理
		long needTime = getMarchNeedTime();
		leaderMarch.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_VALUE);
		getMarchEntity().setEndTime(currentTime + needTime);
		getMarchEntity().setMarchJourneyTime((int) needTime);
		this.updateMarch();

		// 加入集结行军处理
		Set<IWorldMarch> joinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, false);
		for (IWorldMarch joinMarch : joinMarchs) {

			// 已到达队长家的参与者目标点和队长进行同步
			if (joinMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
				WorldMarchService.getInstance().removeWorldPointMarch(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY(), this);
				joinMarch.getMarchEntity().setTerminalId(getMarchEntity().getTerminalId());
				joinMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_JOIN_MARCH_VALUE);
				joinMarch.updateMarch();

			} else {
				// 未到达的行军返回
				double backX = getMarchEntity().getOrigionX();
				double backY = getMarchEntity().getOrigionY();
				AlgorithmPoint currPoint = WorldUtil.getMarchCurrentPosition(joinMarch.getMarchEntity());
				if (currPoint != null) {
					backX = currPoint.getX();
					backY = currPoint.getY();
				}
				// 体力返还
				WorldMarchService.getInstance().onMonsterRelatedMarchAction(joinMarch);
				// 行军返回
				WorldMarchService.getInstance().onMarchReturn(joinMarch, currentTime, getMarchEntity().getAwardItems(), joinMarch.getMarchEntity().getArmys(), backX, backY);
				// 发邮件：队长行军已经出发
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(joinMarch.getPlayerId())
						.setMailId(MailId.MASS_MONSTER_NOT_REACH)
						.build());
			}
		}

		WorldMarchService.logger.info("mass monster march start, march:{}", leaderMarch);
	}

	@Override
	public void onMarchReach(Player leader) {
		// 行军
		WorldMarch leaderMarch = getMarchEntity();
		// 目标点
		int terminalId = leaderMarch.getTerminalId();
		// 目标野怪
		int monsterId = Integer.valueOf(leaderMarch.getTargetId());
		// 点和怪信息
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(terminalId);

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
		
		// 点为空
		if (point == null) {
			massMonsterMarchsReturn();
			for (Player atkPlayer : atkPlayers) {
				sendPointErrorMail(atkPlayer);
			}
			WorldMarchService.logger.error("attack monster march reach error, point null, terminalId:{}", terminalId);
			return;
		}

		// 非野怪点
		if (point.getPointType() != WorldPointType.MONSTER_VALUE && point.getPointType() != WorldPointType.TH_MONSTER_VALUE){
			massMonsterMarchsReturn();
			for (Player atkPlayer : atkPlayers) {
				sendPointErrorMail(atkPlayer);
			}
			WorldMarchService.logger.error("attack monster march reach error, point not monster, terminalId:{}", terminalId);
			return;
		}

		// 野怪点改变
		if (point.getMonsterId() != monsterId) {
			massMonsterMarchsReturn();
			for (Player atkPlayer : atkPlayers) {
				sendPointErrorMail(atkPlayer);
			}
			WorldMarchService.logger.error("attack monster march reach error, point has changed, terminalId:{}", terminalId);
			return;
		}

		// 野怪配置
		WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
		if (monsterCfg == null) {
			massMonsterMarchsReturn();
			sendPointErrorMail(leader);
			WorldMarchService.logger.error("attack monster march reach error, monsterCfg null, monsterId:{}", monsterId);
			return;
		}

		// 野怪类型
		int monsterType = monsterCfg.getType();
		int beforeBlood = point.getRemainBlood();
		
		// 不可被集结攻打的野怪
		if (MonsterType.valueOf(monsterType) == null || monsterType == MonsterType.TYPE_1_VALUE || monsterType == MonsterType.TYPE_2_VALUE) {
			massMonsterMarchsReturn();
			sendPointErrorMail(leader);
			WorldMarchService.logger.error("attack monster march reach error, monster can not be mass, monsterId:{}, monsterType:{}", monsterId, monsterType);
			return;
		}

		/**********************    战斗数据组装及战斗***************************/
		int maxBlood = WorldMonsterService.getInstance().getMaxEnemyBlood(monsterId);
		// 怪物剩余部队万分比
		int leftPercent = (int) (Math.ceil((double)(point.getRemainBlood()) * GsConst.RANDOM_MYRIABIT_BASE / maxBlood));
		PveBattleIncome battleIncome = BattleService.getInstance().initMonsterBattleData(BattleConst.BattleType.ATTACK_MONSTER, point.getId(), monsterCfg.getId(), atkPlayers, atkMarchs, leftPercent <= 0 ? 1 : leftPercent);
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		/**********************    战斗数据组装及战斗***************************/
		// 战斗结果处理
		doBattleResult(leader, point, monsterCfg, atkPlayers, battleOutcome);
		// 发送战斗结果 集结野怪只有胜利
		WorldMarchService.getInstance().sendBattleResultInfo(this, true, WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk()), new ArrayList<ArmyInfo>(), point.getRemainBlood() <= 0);
		// 行军返回
		WorldMarchService.getInstance().onMarchReturn(this, battleOutcome.getAftArmyMapAtk().get(this.getMarchEntity().getPlayerId()), 0);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			WorldMarchService.getInstance().onMarchReturn(massJoinMarch, battleOutcome.getAftArmyMapAtk().get(massJoinMarch.getMarchEntity().getPlayerId()), 0);
		}
		// 战斗胜利，移除点
		if (point.getRemainBlood() <= 0) {
			WorldPointService.getInstance().removeWorldPoint(terminalId);
			
			AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());
			area.removeMonsterBoss(point.getId());
		}
		
		for (Player player : atkPlayers) {
			StatisticsEntity statisticsEntity = player.getData().getStatisticsEntity();
			statisticsEntity.addCommonStatisData(StatisticDataType.GROUP_TOTAL_TODAY, 1);
			
			BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.MASS_MONSTER,
					Params.valueOf("x", point.getX()),
					Params.valueOf("y", point.getY()),
					Params.valueOf("monsterId", monsterCfg.getId()),
					Params.valueOf("monsterLvl", monsterCfg.getLevel()),
					Params.valueOf("isKill", point.getRemainBlood() <= 0),
					Params.valueOf("isLeader", player.getId().equals(leader.getId())));
			
			LogUtil.logAttackMonster(player, point.getX(), point.getY(), monsterCfg.getType(), monsterId, monsterCfg.getLevel(),
					1, 1, beforeBlood, point.getRemainBlood(), point.getRemainBlood() <= 0, false, player.getId().equals(leader.getId()));
			
			GuildRankMgr.getInstance().onPlayerKillMonster(player.getId(), player.getGuildId(),1 );
		}
		
		// 刷新战力
//		refreshPowerAfterWar(atkPlayers, null);
		
		try {
			for (IWorldMarch march : atkMarchs) {
				Player marchlPlayer = march.getPlayer();
				boolean killLimit = this.checkBossKillLimitReward(marchlPlayer, monsterCfg);
				if(killLimit){
					//如果受限制，则返回体力
					marchlPlayer.dealMsg(MsgId.RETURN_VIT, new MarchVitReturnBackMsgInvoker(marchlPlayer, march));
				}else{
					ActivityManager.getInstance().postEvent(new VitCostEvent(march.getPlayerId(), march.getMarchEntity().getVitCost(), true));
					HawkApp.getInstance().postMsg(marchlPlayer, PlayerVitCostMsg.valueOf(march.getPlayerId(), march.getMarchEntity().getVitCost()));
				}
				//添加参与次数
				if(ConstProperty.getInstance().inBossLimit185(monsterCfg.getId())){
					marchlPlayer.getData().getMonsterEntity().addBossKill(monsterCfg.getId(), 1);
					marchlPlayer.getPush().syncMonsterKillData();
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		//185活动专用
		ActivityManager.getInstance().postEvent(new MonsterBossAttackEvent(leader.getId(),
				monsterCfg.getType(), monsterCfg.getId(), monsterCfg.getLevel(), 1, true));
	}

	/**
	 * 战斗结果处理(奖励、邮件处理)
	 * @param leader
	 * @param point
	 * @param monsterCfg
	 * @param atkPlayers
	 * @param isAtkWin
	 */
	private void doBattleResult(Player leader, WorldPoint point, WorldEnemyCfg monsterCfg, List<Player> atkPlayers, BattleOutcome battleOutcome) {
		// 总击杀怪物数量(血量)
		int totalKillCount = getTotalKillMonsterCount(battleOutcome);

		// 设置怪物剩余血量
		int remainBlood = calcAfterBlood(point, totalKillCount);
		point.setRemainBlood(remainBlood > 0 ? remainBlood : 0);
		List<String> atkPlayerIds = new ArrayList<>();
		// 发伤害奖励
		for (Player player : atkPlayers) {
			boolean bossKillLimit = this.checkBossKillLimitReward(player, monsterCfg);
			// 计算伤害奖励
			calcAndSendAtkAward(point, monsterCfg, totalKillCount, battleOutcome, player, remainBlood,bossKillLimit);
			boolean isLeader = player.getId().equals(leader.getId());
			// pve事件
			postEventAfterPve(player, battleOutcome, this.getMarchType(), monsterCfg.getLevel());
			if(!bossKillLimit){
				// 野怪攻打时间 //集结只会打精英怪
				ActivityManager.getInstance()
				.postEvent(new MonsterAttackEvent(player.getId(), monsterCfg.getType(), monsterCfg.getId(), monsterCfg.getLevel(), 1, remainBlood <= 0, false, isLeader));
				//如果次数够了，回流那个活动也不参加了
				atkPlayerIds.add(player.getId());
			}
		}
		try {
			ActivityService.getInstance().dealMsg(GameConst.MsgId.ON_GUILD_BACK_DROP, new GuildBackDropInvoker(atkPlayerIds));
		}catch (Exception e){
			HawkException.catchException(e);
		}
		// 战斗胜利 发击杀奖励
		if (remainBlood <= 0) {
			// 发邮件：击杀奖励邮件
			sendKillAwardMail(point, monsterCfg, atkPlayers, battleOutcome);
			// 发邮件：击杀联盟奖励邮件
			sendKillGuildAwardMail(leader, point, monsterCfg, battleOutcome);
			// 联盟任务-击杀野怪
			GuildService.getInstance().postGuildTaskMsg(new KillMonsterTaskEvent(leader.getGuildId()));
		}
	}
	
	public boolean checkBossKillLimitReward(Player player,WorldEnemyCfg monsterCfg){
		//检查是否击杀次数到上限
		ImmutableMap<Integer,List<Integer>> bossList = ConstProperty.getInstance().getBossEnemyId185();
		for(Map.Entry<Integer,List<Integer>> entry : bossList.entrySet()){
			int groupIndex = entry.getKey();
			List<Integer> blist = entry.getValue();
			if(blist.contains(monsterCfg.getId())){
				int count = player.getData().getMonsterEntity().getBossKillCountDaily(blist);
				int bossKillLimitCount = ConstProperty.getInstance().getBossDailyLootTimeLimit185(groupIndex);
				if(count >= bossKillLimitCount){
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * 获取击杀总数量
	 * @param battleOutcome
	 * @return
	 */
	private int getTotalKillMonsterCount(BattleOutcome battleOutcome) {
		int totalKillCount = 0;
		Map<String, List<ArmyInfo>> aftArmyMapAtk = battleOutcome.getAftArmyMapAtk();
		for (List<ArmyInfo> armyInfos : aftArmyMapAtk.values()) {
			for (ArmyInfo armyInfo : armyInfos) {
				totalKillCount += armyInfo.getKillCount();
			}
		}
		return totalKillCount;
	}

	/**
	 * 计算伤害奖励
	 * @param monsterCfg
	 * @param totalKillCount
	 * @param aftArmyMapAtk
	 * @param player
	 * @return
	 */
	private AwardItems calcAndSendAtkAward(WorldPoint point, WorldEnemyCfg monsterCfg, int totalKillCount, BattleOutcome battleOutcome, Player player, int remainBlood,boolean bossKillLimit) {
		AwardItems atkAward = AwardItems.valueOf();
		int maxEnemyBlood = WorldMonsterService.getInstance().getMaxEnemyBlood(monsterCfg.getId());
		int killRate = (int) (Math.ceil(getKillRate(battleOutcome, player, maxEnemyBlood)));
		killRate = killRate > 0 ? killRate : 1;
		
		// 修正系数
		boolean fixed = false;
		
		int allKillRate = getAllKillRate(battleOutcome, monsterCfg.getId());
		int fixRate = (int)Math.ceil(allKillRate * WorldMapConstProperty.getInstance().getYuriBossDamageRewardParam() * GsConst.EFF_PER);
		if (killRate > fixRate) {
			killRate = fixRate;
			fixed = true;
		}
		// 攻打野怪奖励,如果达到上限是不发奖励
		if(!bossKillLimit){
			List<Integer> damageAwards = monsterCfg.getDamageAwards();
			for (Integer damageAward : damageAwards) {
				for (int i = 0; i < killRate; i++) {
					atkAward.addAward(damageAward);
				}
			}
		}
		
		// 发邮件：伤害奖励邮件
		sendAtkAward(point, monsterCfg, battleOutcome, player, atkAward, remainBlood, totalKillCount, fixed,bossKillLimit);
		
		return atkAward;
	}

	/**
	 * 集结打怪行军返回
	 */
	private void massMonsterMarchsReturn() {
		// 体力返还
		WorldMarchService.getInstance().onMonsterRelatedMarchAction(this);
		// 队长行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());

		// 队员行军返回
		Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			// 体力返还
			WorldMarchService.getInstance().onMonsterRelatedMarchAction(massJoinMarch);
			// 行军返回
			WorldMarchService.getInstance().onMarchReturn(massJoinMarch, massJoinMarch.getMarchEntity().getArmys(), getMarchEntity().getTerminalId());
		}
	}

	/**
	 * 发邮件：目标野怪消失
	 * @param leader
	 */
	private void sendPointErrorMail(Player leader) {
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(leader.getId())
				.setMailId(MailId.MASS_MONSTER_DISAPPERA)
				.build());
	}
	
	/**
	 * 发邮件：击杀奖励邮件
	 * @param point
	 * @param monsterCfg
	 * @param atkPlayers
	 * @param battleOutcome
	 */
	private void sendKillAwardMail(WorldPoint point, WorldEnemyCfg monsterCfg, List<Player> atkPlayers, BattleOutcome battleOutcome) {
		for (Player player : atkPlayers) {
			//检查是否击杀次数到上限
			boolean bossKillLimit = this.checkBossKillLimitReward(player, monsterCfg);
			if(bossKillLimit){
				continue;
			}
			// 击杀奖励
			AwardItems killAward = AwardItems.valueOf();
			killAward.addAwards(monsterCfg.getKillAwards());
			
			// 能量探测器翻倍
			if (monsterCfg.getId() == 600004) {
				long buffEndTime = player.getData().getBuffEndTime(EffType.ATK_MONSTER_ENERGY_DETECTOR_VALUE);
				if (buffEndTime > HawkTime.getMillisecond()) {
					for (ItemInfo award : killAward.getAwardItems()) {
						boolean isEnergyDetectorTool = WorldMarchConstProperty.getInstance().isEnergyDetectorTool(award.getItemId());
						if (award.getItemType() != ItemType.TOOL || !isEnergyDetectorTool) {
							continue;
						}
						int energyDetectorMultipleEffect = WorldMarchConstProperty.getInstance().getEnergyDetectorMultipleEffect();
						award.setCount(award.getCount() * energyDetectorMultipleEffect);
					}
				}
			}
			
			// 发邮件
			MonsterMail.Builder mailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, Collections.emptyList(), monsterCfg.getId(), point, killAward.getAwardItems(), null, 0, 0, 0);
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.MASS_MONSTER_KILL_AWARD)
					.addContents(mailBuilder)
					.setRewards(killAward.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addTips(monsterCfg.getId())
					.build());
		}
	}

	/**
	 * 发邮件：联盟击杀奖励邮件
	 * @param leader
	 * @param point
	 * @param monsterCfg
	 * @param battleOutcome
	 */
	private void sendKillGuildAwardMail(Player leader, WorldPoint point, WorldEnemyCfg monsterCfg, BattleOutcome battleOutcome) {
		//检查是否击杀次数到上限
		boolean killLimit = this.checkBossKillLimitReward(leader, monsterCfg);
		if(killLimit){
			return;
		}
		// 联盟奖励
		AwardItems guildAward = AwardItems.valueOf();
		guildAward.addAwards(monsterCfg.getUnionAwards());
		for (String playerId : GuildService.getInstance().getGuildMembers(leader.getGuildId())) {
			// 发邮件
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(playerId)
					.setMailId(MailId.MASS_MONSTER_KILL_GUILD_AWARD)
					.setRewards(guildAward.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.build());
		}
	}

	/**
	 * 发邮件：伤害奖励邮件
	 * @param point
	 * @param monsterCfg
	 * @param battleOutcome
	 * @param player
	 * @param atkAward
	 */
	private void sendAtkAward(WorldPoint point, WorldEnemyCfg monsterCfg, BattleOutcome battleOutcome, Player player, AwardItems atkAward, int remainBlood, int totalKillCount, boolean fixed,boolean killCountLimit) {
		// 获取怪物最大血量
		int totalEnemyBlood = WorldMonsterService.getInstance().getMaxEnemyBlood(monsterCfg.getId());
		// 获取伤害比率
		float killRate = getKillRate(battleOutcome, player, totalEnemyBlood) / 100;
		// 获取受伤部队数量
		int woundCount = getWoundCount(battleOutcome, player);
		// 发邮件:伤害奖励
		MonsterMail.Builder monsterMailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, Collections.emptyList(), monsterCfg.getId(), point, atkAward.getAwardItems(), null, (float) remainBlood / totalEnemyBlood, killRate, woundCount);
		monsterMailBuilder.setFixed(fixed);
		monsterMailBuilder.setKillCntLimit(killCountLimit);
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(battleOutcome.isAtkWin() ? MailId.MASS_MONSTER_AWARD_WIN : MailId.MASS_MONSTER_AWARD_FAIL)
				.addContents(monsterMailBuilder)
				.setRewards(atkAward.getAwardItems())
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.build());
	}

	/**
	 * 获取受伤部队数量
	 * @param battleOutcome
	 * @param player
	 * @return
	 */
	private int getWoundCount(BattleOutcome battleOutcome, Player player) {
		int woundCount = 0;
		List<ArmyInfo> playerArmyInfos = battleOutcome.getAftArmyMapAtk().get(player.getId());
		for (ArmyInfo playerArmyInfo : playerArmyInfos) {
			woundCount += playerArmyInfo.getWoundedCount();
		}
		return woundCount;
	}

	/**
	 * 获取伤害比率
	 * @param battleOutcome
	 * @param player
	 * @param totalCount
	 * @return
	 */
	private float getKillRate(BattleOutcome battleOutcome, Player player, int totalCount) {
		// 单人击杀玩家数量
		int playerKillCount = 0;
		List<ArmyInfo> playerArmyInfos = battleOutcome.getAftArmyMapAtk().get(player.getId());
		for (ArmyInfo playerArmyInfo : playerArmyInfos) {
			playerKillCount += playerArmyInfo.getKillCount();
		}
		// 伤害比率
		float killRate = (float) playerKillCount / totalCount * 100;
		return killRate;
	}

	public int getAllKillRate(BattleOutcome battleOutcome, int monsterId) {
		int killBlood = 0;
		for (List<ArmyInfo> playerArmyInfos : battleOutcome.getAftArmyMapAtk().values()) {
			for (ArmyInfo playerArmyInfo : playerArmyInfos) {
				killBlood += playerArmyInfo.getKillCount();
			}
		}
		
		int maxBlood = WorldMonsterService.getInstance().getMaxEnemyBlood(monsterId);
		killBlood = Math.min(maxBlood, killBlood);
		
		// 伤害比率
		float killRate = (float) killBlood / killBlood * 100;
		return (int) (Math.ceil(killRate));
	}
	
	/**
	 * 计算怪物剩余血量(部队)
	 * @param point
	 * @param totalKillCount
	 * @return
	 */
	private int calcAfterBlood(WorldPoint point, int totalKillCount) {
		// 攻打前怪物剩余血量
		int beforeBlood = point.getRemainBlood();
		// 攻击后怪物剩余血量
		int afterBlood = (beforeBlood >= totalKillCount) ? (beforeBlood - totalKillCount) : 0;
		return afterBlood;
	}
	
	/**
	 * 获取被动方联盟战争界面信息
	 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		
		int terminalId = this.getMarchEntity().getTerminalId();
		int[] pos = GameUtil.splitXAndY(terminalId);
		builder.setPointType(WorldPointType.MONSTER);
		builder.setX(pos[0]);
		builder.setY(pos[1]);

		int targetId = Integer.parseInt(this.getMarchEntity().getTargetId());
		builder.setMonsterId(targetId);
		return builder;
	}
	
	@Override
	public boolean needShowInGuildWar() {
		return true;
	}
}
