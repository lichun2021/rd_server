package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.constant.ActivityConst;
import com.hawk.activity.event.impl.AgencyMonsterAtkWinEvent;
import com.hawk.activity.event.impl.HonorItemDropEvent;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.event.impl.PowerLabItemDropEvent;
import com.hawk.activity.event.impl.VitCostEvent;
import com.hawk.game.GsApp;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.AgencyConstCfg;
import com.hawk.game.config.AgencyEventCfg;
import com.hawk.game.config.AgencyLevelCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.entity.AgencyEntity;
import com.hawk.game.entity.AgencyEventEntity;
import com.hawk.game.entity.PlayerMonsterEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.guildrank.GuildRankMgr;
import com.hawk.game.invoker.MarchVitReturnBackMsgInvoker;
import com.hawk.game.invoker.MonsterAtkAwardMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.msg.AgencyFinishMsg;
import com.hawk.game.msg.AtkAfterPveMsg;
import com.hawk.game.msg.PlayerVitCostMsg;
import com.hawk.game.msg.SuperSoldierTriggeTaskMsg;
import com.hawk.game.msg.TimeLimitStoreTriggerMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.skill.talent.Skill10304;
import com.hawk.game.player.skill.talent.TalentSkillContext;
import com.hawk.game.protocol.Agency.AgencyEventState;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.Mail.MonsterMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SuperSoldier.SupersoldierTaskType;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.PushService;
import com.hawk.game.service.guildtask.event.KillMonsterTaskEvent;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mail.YuriMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventMonsterAttack;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.TimeLimitStoreTriggerType;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldMonsterService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.Source;

public class AgencyMonsterMarch extends PlayerMarch implements BasedMarch {
	private BattleOutcome battleOutcome;
	
	public AgencyMonsterMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public void onMarchStart() {
		try {
			battleOutcome = dobattle();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.AGENCY_MARCH_MONSTER;
	}
	
	@Override
	public double getPartMarchTime(double distance, double speed, boolean isSlowDownPart) {
		AgencyEntity agency = this.getPlayer().getData().getAgencyEntity();
		int agencyLevel = agency.getCurrLevel();
		AgencyLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AgencyLevelCfg.class, agencyLevel);
		if(cfg != null){
			double factor = 1;
			if (isSlowDownPart) {
				factor = WorldMarchConstProperty.getInstance().getWorldMarchCoreRangeTime();
			}
			// 行军距离修正参数
			double param1 = WorldMarchConstProperty.getInstance().getDistanceAdjustParam();
			// 部队行军类型行军时间调整参数
			double param2 = cfg.getTimeModulus3();
			//speed = 1.0d;
			return Math.pow((distance), param1) * param2 * factor / speed;
		}
		return super.getPartMarchTime(distance, speed, isSlowDownPart);
	}

	
	@Override
	public long getMarchNeedTime() {
		if (this.getPlayer().getData().getAgencyEntity().getHasKilled() == 0) {
			return AgencyConstCfg.getInstance().getFirstEventTime() * 1000;
		}
		return super.getMarchNeedTime();
	}
	
	
	private BattleOutcome dobattle() {
		if(Objects.nonNull(battleOutcome)){
			return battleOutcome;
		}
		
		// 行军
		WorldMarch march = getMarchEntity();
		// 目标点
		int terminalId = march.getTerminalId();
		// 情报事件
		AgencyEventEntity agencyEvent = this.getPlayer().getData().getAgencyEntity().getAgencyEvent(march.getTargetId());
		if (agencyEvent == null) {
			return null;
		}
		// 情报事件配置
		AgencyEventCfg agencyEventCfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class, agencyEvent.getEventId());
		
		// 野怪配置
		WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, agencyEventCfg.getDifficulty());
		if (monsterCfg == null) {
			return null;
		}
		
		Player player = getPlayer();
		// 组织战斗数据
		List<Player> atkPlayers = new ArrayList<>();
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);
		atkPlayers.add(player);

		// 战斗
		PveBattleIncome battleIncome = BattleService.getInstance().initMonsterBattleData(BattleConst.BattleType.ATTACK_MONSTER, terminalId, monsterCfg.getId(), atkPlayers, atkMarchs);
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		return battleOutcome;
	}

	@Override
	public void onMarchReach(Player player) {
		// 行军
		WorldMarch march = getMarchEntity();
		// 目标点
		int terminalId = march.getTerminalId();
		// 点和怪信息
		WorldPoint wpoint = WorldPointService.getInstance().getWorldPoint(terminalId);
		// 点被占用
		if (wpoint != null) {
			WorldMarchService.getInstance().onMonsterRelatedMarchAction(this);
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			this.sendAgencyPointChangeEmail(player);
			return;
		}
		
		int[] pos = GameUtil.splitXAndY(terminalId);
		Point freePoint = WorldPointService.getInstance().getAreaPoint(pos[0], pos[1], true);
		if (freePoint == null) {
			WorldMarchService.getInstance().onMonsterRelatedMarchAction(this);
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			this.sendAgencyPointChangeEmail(player);
			return;
		}
		
		WorldPoint point = new WorldPoint();
		point.setPointType(WorldPointType.EMPTY_VALUE);
		point.setX(pos[0]);
		point.setY(pos[1]);

		// 情报事件
		AgencyEventEntity agencyEvent = this.getPlayer().getData().getAgencyEntity().getAgencyEvent(march.getTargetId());
		if (agencyEvent == null) {
			WorldMarchService.getInstance().onMonsterRelatedMarchAction(this);
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			this.sendAgencyPointChangeEmail(player);
			return;
		}
		this.getPlayer().getData().getAgencyEntity().setHasKilled(1);
		// 情报事件配置
		AgencyEventCfg agencyEventCfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class, agencyEvent.getEventId());
		// 野怪配置
		WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, agencyEventCfg.getDifficulty());
		if (monsterCfg == null) {
			WorldMarchService.getInstance().onMonsterRelatedMarchAction(this);
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			this.sendAgencyPointChangeEmail(player);
			return;
		}
		// 事件已完成
		if (agencyEvent.getEventState() == AgencyEventState.AGENCY_FINISHED_VALUE) {
			WorldMarchService.getInstance().onMonsterRelatedMarchAction(this);
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			return;
		}
		
		// 战斗
		BattleOutcome battleOutcome = dobattle();
		// 战斗结果
		boolean isWin = battleOutcome.isAtkWin();
		List<ArmyInfo> afterArmyList = WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk());
		// 发送战斗结果
		WorldMarchService.getInstance().sendBattleResultInfo(this, isWin, afterArmyList, new ArrayList<ArmyInfo>(), isWin);
		// 行军返回
		WorldMarchService.getInstance().onMarchReturn(this, WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk()), 0);
		// 更新击杀野怪等级
		boolean isFirst = false;
		if (monsterCfg.getLevel() > player.getData().getMonsterEntity().getMaxLevel()) {
			isFirst = true;
		}

		//TODO 合并处理开始
		int atkTimes = 1;
		if (isWin) {
			Skill10304 talentSkill = TalentSkillContext.getInstance().getSkill(Skill10304.SKILL_ID);
			if(talentSkill.touchSkill(player, null)){
				int heiha = talentSkill.getAtkTimesAndCostVitAndRemoveSkillBuff(player, monsterCfg, this.getMarchEntity().getEffectParams(), march);
				atkTimes += heiha;
			}
		}
		
		// 结果处理
		doAtkMonsterResult(point, player, battleOutcome, march.getHeroIdList(), monsterCfg, atkTimes);
		if (isWin) {
			WorldMonsterService.getInstance().notifyMonsterKilled(point);
			HawkApp.getInstance().postMsg(player, AgencyFinishMsg.valueOf(agencyEvent.getUuid()));
		} else if (agencyEvent.getIsSpecialEvent() == 1) {
			// 金色打野事件，失败的话返回体力
			getPlayer().dealMsg(MsgId.RETURN_VIT, new MarchVitReturnBackMsgInvoker(getPlayer(), this));
		}
		
		////////////////////////////////////////////////////////////////// 原来的处理
//		doAtkMonsterResult(point, player, battleOutcome, march.getHeroIdList(), monsterCfg, false);
//		// 通知野怪击杀，移除野怪点
//		if (isWin) {
//			Skill10304 talentSkill = TalentSkillContext.getInstance().getSkill(Skill10304.SKILL_ID);
//			if(talentSkill.touchSkill(player, null)){
//				int heiha = talentSkill.getAtkTimesAndCostVitAndRemoveSkillBuff(player, monsterCfg, this.getMarchEntity().getEffectParams(), march);
//				atkTimes = heiha;
//				for (int i = 0; i < heiha; i++) {
//					GsApp.getInstance().addDelayAction(i * 50, new HawkDelayAction() {// 解决客户端一次处理不了太多协议
//						@Override
//						protected void doAction() {
//							// 结果处理
//							doAtkMonsterResult(point, player, battleOutcome, march.getHeroIdList(), monsterCfg, true);
//						}
//					});
//				}
//			}
//			WorldMonsterService.getInstance().notifyMonsterKilled(point);
//			HawkApp.getInstance().postMsg(player, AgencyFinishMsg.valueOf(agencyEvent.getUuid()));
//		} else {
//			// 金色打野事件，失败的话返回体力
//			if (agencyEvent.getIsSpecialEvent() == 1) {
//				getPlayer().dealMsg(MsgId.RETURN_VIT, new MarchVitReturnBackMsgInvoker(getPlayer(), this));
//			}
//		}
		///////////////////////////////////////////////////////////////////////////
		
		
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_REACH_FIGHT_MONSTER, Params.valueOf("marchData", march),
				Params.valueOf("atkLeftArmyList", afterArmyList), Params.valueOf("isWin", isWin));
		// 日志
		WorldMarchService.logger.info("world attack monster isWin: {}, playerId: {}, atkLeftArmyList: {}", isWin, player.getId(), afterArmyList);

		LogUtil.logAttackMonster(player, point.getX(), point.getY(), monsterCfg.getType(), monsterCfg.getId(), monsterCfg.getLevel(), atkTimes, atkTimes, 0, 0, isWin, isFirst, true);
		
		GuildRankMgr.getInstance().onPlayerKillMonster(player.getId(), player.getGuildId(), 1);
		// 刷新战力
//		refreshPowerAfterWar(Arrays.asList(player), null);
		
		// 触发限时商店
		HawkApp.getInstance().postMsg(player, new TimeLimitStoreTriggerMsg(TimeLimitStoreTriggerType.ATTACK_MONSTER, 1));
		
		try {
			if (isWin || (!isWin && agencyEvent.getIsSpecialEvent() != 1)) {
				ActivityManager.getInstance().postEvent(new AgencyMonsterAtkWinEvent(march.getPlayerId(), atkTimes));
				ActivityManager.getInstance().postEvent(new VitCostEvent(march.getPlayerId(), march.getVitCost()));
				HawkApp.getInstance().postMsg(getPlayer(), PlayerVitCostMsg.valueOf(march.getPlayerId(), march.getVitCost()));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	@Override
	public void onWorldMarchReturn(Player player) {
		// 打怪部队返回推送
		PushService.getInstance().pushMsg(player.getId(), PushMsgType.ACTTACK_BOSS_ARMY_RETURN_VALUE);
	}
	
	/**
	 * 攻打野怪战后数据处理
	 * @param point
	 * @param player
	 * @param battleOutcome
	 * @param heroId
	 * @param monsterCfg
	 */
	public void doAtkMonsterResult(WorldPoint point, Player player, BattleOutcome battleOutcome, List<Integer> heroId, WorldEnemyCfg monsterCfg, boolean useSkill) {
		// 战斗是否胜利
		boolean isWin = battleOutcome.isAtkWin();
		// 更新打怪任务、活动、统计
		missionRefresh(player, isWin, monsterCfg.getLevel(), monsterCfg.getId(), monsterCfg.getType(), heroId);
		GsApp.getInstance().postMsg(player.getXid(), AtkAfterPveMsg.valueOf(battleOutcome, this.getMarchType(), monsterCfg.getLevel())); //6.胜利可以不用抛事件
		
		if (!isWin) {
			MonsterMail.Builder mailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, heroId, monsterCfg.getId(), point, null, null, 0, 0, 0);
			YuriMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.MONSTER_FAILED)
					.addContents(mailBuilder).addTips(monsterCfg.getId()).build());
			return;
		}
		// 更新击杀野怪等级
		PlayerMonsterEntity monsterEntity = player.getData().getMonsterEntity();
		int killedLvl = monsterEntity.getMaxLevel();
		if (monsterCfg.getLevel() > killedLvl) {
			monsterEntity.setMaxLevel(monsterCfg.getLevel());
		}
		player.getPush().syncMonsterKilled(monsterCfg.getId(), isWin); //7.待客户端确认

		// 击杀奖励
		AwardItems killAward = AwardItems.valueOf();
		killAward.addAwards(monsterCfg.getKillAwards());
		
		// 击杀额外奖励 -- 376
		int extrAward376 = player.getEffect().getEffVal(EffType.MONSTER_EXTR_AWARD, getMarchEntity().getEffectParams()) / 10000;
		for (int i = 0; i < extrAward376; i++) {
			killAward.addAwards(monsterCfg.getKillAwards());
		}

		// 击杀额外随机奖励 -- 376
		int extrRandomAward376 = player.getEffect().getEffVal(EffType.MONSTER_EXTR_AWARD, getMarchEntity().getEffectParams()) % 10000;
		if (HawkRand.randInt(10000) < extrRandomAward376) {
			killAward.addAwards(monsterCfg.getKillAwards());
		}
		// 击杀额外随机奖励 -- 338
		int extrRandomAward338 = player.getEffect().getEffVal(EffType.EFF_338, getMarchEntity().getEffectParams()) % 10000;
		if (HawkRand.randInt(10000) < extrRandomAward338) {
			killAward.addAwards(monsterCfg.getKillAwards());
		}
		int extrRandomAward377 = player.getEffect().getEffVal(EffType.MONSTER_EXTR_AWARD_HERO, getMarchEntity().getEffectParams());
		if (HawkRand.randInt(10000) < extrRandomAward377) {
			int effect377LinkToAwardId = ConstProperty.getInstance().getEffect377LinkToAwardId();
			if (effect377LinkToAwardId != 0) {
				killAward.addAward(effect377LinkToAwardId);
			}
		}
		// 指挥官经验
		double exp500 = player.getEffect().getEffVal(EffType.PLAYER_EXP_PER, getMarchEntity().getEffectParams()) * GsConst.EFF_PER;
		double exp515 = player.getEffect().getEffVal(EffType.PLAYER_EXP_PER_MONSTER, getMarchEntity().getEffectParams()) * GsConst.EFF_PER;
		double exp337 = player.getEffect().getEffVal(EffType.EFF_337) * GsConst.EFF_PER;
		int addExp = (int)(monsterCfg.getCommanderExp() * (1 + exp500 + exp515 + exp337));
		killAward.appendAward(AwardItems.valueOf().addExp(addExp));
		// 首杀奖励
		AwardItems firstKillAward = AwardItems.valueOf();
		if (monsterCfg.getLevel() > killedLvl) {
			if (monsterCfg.getFirstKillaward() != 0) {
				firstKillAward.addAward(monsterCfg.getFirstKillaward());
			}
		}
		// eff:4022
		int buff = player.getEffect().getEffVal(EffType.KILL_MONSTER_AWARD_ADD, getMarchEntity().getEffectParams());
		if (useSkill) {
			buff = 0;
		}
		for (ItemInfo award : killAward.getAwardItems()) {
			int count = (int)(award.getCount() * (1 + buff * GsConst.EFF_PER));
			award.setCount(count);
		}
		// 能量探测器翻倍
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
		//回归特权 eff:28102   奖励翻倍,多添加一次原始奖励和经验
		int addVal =  player.getEffect().getEffVal(EffType.BACK_PRIVILEGE_ATK_MONSTER_AWARD_DOUBLE_TIMES);
		if(addVal > 0){
			int effTimes = RedisProxy.getInstance().effectTodayUsedTimes(player.getId(), EffType.BACK_PRIVILEGE_ATK_MONSTER_AWARD_DOUBLE_TIMES);
			if(effTimes < addVal){
				//指挥官经验
				killAward.appendAward(AwardItems.valueOf().addExp(monsterCfg.getCommanderExp()));
				//道具
				killAward.addAwards(monsterCfg.getKillAwards());
				RedisProxy.getInstance().effectTodayUseInc(player.getId(), EffType.BACK_PRIVILEGE_ATK_MONSTER_AWARD_DOUBLE_TIMES, 1);
			}
		}
		for (ItemInfo award : killAward.getAwardItems()) {
			boolean isEnergyDetectorTool = WorldMarchConstProperty.getInstance().isEnergyDetectorTool(award.getItemId());
			if (award.getItemType() == ItemType.TOOL) {
				if(isEnergyDetectorTool){
					ActivityManager.getInstance().postEvent(new PowerLabItemDropEvent(player.getId(), award.getItemId(), (int) award.getCount())); //8.合并count数量即可
				}
				if(award.getItemId() == ActivityConst.HONOR_ITEMID){
					ActivityManager.getInstance().postEvent(new HonorItemDropEvent(player.getId(), (int) award.getCount()));  //9.合并count数量即可
				}
			}
		}
		// 发邮件
		MonsterMail.Builder mailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, heroId, monsterCfg.getId(), point, killAward.getAwardItems(), firstKillAward.getAwardItems(), 0, 0, 0);
		YuriMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.MONSTER_SUCC)
				.addContents(mailBuilder).addTips(monsterCfg.getId()).build()); //10.需要策划提供新的邮件模板
		// 投递发奖
		player.dealMsg(MsgId.ATTACK_MONSTER_AWARD, new MonsterAtkAwardMsgInvoker(player, monsterCfg.getId(), killAward, firstKillAward, AwardItems.valueOf(), 1)); //11.直接合并奖励即可
	}

	
	/**
	 * 攻打野怪活动、任务数据刷新
	 * @param atkPlayer
	 * @param isAtkWin
	 * @param monsterLevel
	 * @param monsterId
	 * @param mosterType
	 */
	public void missionRefresh(Player atkPlayer, final boolean isAtkWin, final int monsterLevel, int monsterId, int mosterType, List<Integer> heroIds) {
		MissionManager.getInstance().postMsg(atkPlayer, new EventMonsterAttack(monsterId, monsterLevel, isAtkWin)); //1.把次数带过去即可
		MissionManager.getInstance().postSuperSoldierTaskMsg(atkPlayer, new SuperSoldierTriggeTaskMsg(SupersoldierTaskType.ATTACK_MONSTER_TASK, 1)); //2.把次数带过去即可	
		atkPlayer.getData().getStatisticsEntity().addAtkMonsterCnt(1);
		MonsterAttackEvent event = new MonsterAttackEvent(atkPlayer.getId(), mosterType, monsterId, monsterLevel, 1, isAtkWin, !heroIds.isEmpty());
		ActivityManager.getInstance().postEvent(event); //3.绝大部分是把次数带过去即可，要过一遍所有用到的地方，针对处理
		// 跨服消息投递-野怪击杀
		CrossActivityService.getInstance().postEvent(event); //4.AttackMonsterTargetParser类中的逻辑循环执行n次
		if (isAtkWin) {
			// 击杀野怪数量增加
			atkPlayer.getData().getStatisticsEntity().addAtkMonsterWinCnt(1);
			// 联盟任务-击杀野怪
			GuildService.getInstance().postGuildTaskMsg(new KillMonsterTaskEvent(atkPlayer.getGuildId())); //5.把次数带过去即可
		}
	}
	
	@Override
	public void moveCityProcess(long currentTime) {
		WorldMarchService.getInstance().accountMarchBeforeRemove(this);
		
		if (!this.isReturnBackMarch()) {
			// 打怪行军
			WorldMarchService.getInstance().onMonsterRelatedMarchAction(this);
		}
	}
	
	
	private void sendAgencyPointChangeEmail(Player player){
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.AGENCY_POINT_NULL)
				.build());
	}
	
	/**
	 * 打野结算处理（合并）
	 * @param point
	 * @param player
	 * @param battleOutcome
	 * @param heroId
	 * @param monsterCfg
	 * @param atkTimes
	 */
	public void doAtkMonsterResult(WorldPoint point, Player player, BattleOutcome battleOutcome, List<Integer> heroId, WorldEnemyCfg monsterCfg, int atkTimes) {
		// 战斗是否胜利
		boolean isWin = battleOutcome.isAtkWin();
		// 更新打怪任务、活动、统计
		MissionManager.getInstance().postMsg(player, new EventMonsterAttack(monsterCfg.getId(), monsterCfg.getLevel(), isWin, atkTimes)); // 1 -- 把次数带过去即可
		MissionManager.getInstance().postSuperSoldierTaskMsg(player, new SuperSoldierTriggeTaskMsg(SupersoldierTaskType.ATTACK_MONSTER_TASK, atkTimes)); // 2 -- 把次数带过去即可	
		player.getData().getStatisticsEntity().addAtkMonsterCnt(atkTimes);
		MonsterAttackEvent event = new MonsterAttackEvent(player.getId(), monsterCfg.getType(), monsterCfg.getId(), monsterCfg.getLevel(), atkTimes, isWin, !heroId.isEmpty());
		ActivityManager.getInstance().postEvent(event); // 3 -- 绝大部分是把次数带过去即可，要过一遍所有用到的地方，针对处理
		// 跨服消息投递-野怪击杀
		CrossActivityService.getInstance().postEvent(event); // 4 -- AttackMonsterTargetParser类中的逻辑循环执行n次
		if (isWin) {
			// 击杀野怪数量增加
			player.getData().getStatisticsEntity().addAtkMonsterWinCnt(atkTimes);
			// 联盟任务-击杀野怪
			GuildService.getInstance().postGuildTaskMsg(new KillMonsterTaskEvent(player.getGuildId(), atkTimes)); // 5 -- 把次数带过去即可
		} else {
			GsApp.getInstance().postMsg(player.getXid(), AtkAfterPveMsg.valueOf(battleOutcome, this.getMarchType(), monsterCfg.getLevel())); // 6 -- 胜利可以不用抛事件
			MonsterMail.Builder mailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, heroId, monsterCfg.getId(), point, null, null, 0, 0, 0);
			YuriMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.MONSTER_FAILED)
					.addContents(mailBuilder).addTips(monsterCfg.getId()).build());
			return;
		}
		
		// 更新击杀野怪等级
		PlayerMonsterEntity monsterEntity = player.getData().getMonsterEntity();
		int killedLvl = monsterEntity.getMaxLevel();
		if (monsterCfg.getLevel() > killedLvl) {
			monsterEntity.setMaxLevel(monsterCfg.getLevel());
		}
		player.getPush().syncMonsterKilled(monsterCfg.getId(), isWin); //7 -- 待客户端确认

		long now = HawkTime.getMillisecond();
		int effTimes = -1, addTimes = 0;
		// 击杀奖励
		List<AwardItems> killAwardList = new ArrayList<>();
		for (int i = 0; i < atkTimes; i++) {
			//掉落记录
			Map<Integer, Integer>  dropLimitMap = monsterEntity.getDropLimitMap();
			//是否被限制掉落
			List<Integer> monsterKillAward = ConstProperty.getInstance().checkBlazeMedalLimitAward(dropLimitMap, monsterCfg.getKillAwards());

			AwardItems killAward = AwardItems.valueOf();
			killAwardList.add(killAward);
			killAward.addAwards(monsterKillAward);

			// 击杀额外奖励 -- 376
			int extrAward376 = player.getEffect().getEffVal(EffType.MONSTER_EXTR_AWARD, getMarchEntity().getEffectParams()) / 10000;
			for (int j = 0; j < extrAward376; j++) {
				killAward.addAwards(monsterKillAward);
			}
			
			// 击杀额外随机奖励 -- 376
			int extrRandomAward376 = player.getEffect().getEffVal(EffType.MONSTER_EXTR_AWARD, getMarchEntity().getEffectParams()) % 10000;
			if (HawkRand.randInt(10000) < extrRandomAward376) {
				killAward.addAwards(monsterKillAward);
			}
			// 击杀额外随机奖励 -- 338
			int extrRandomAward338 = player.getEffect().getEffVal(EffType.EFF_338, getMarchEntity().getEffectParams()) % 10000;
			if (HawkRand.randInt(10000) < extrRandomAward338) {
				killAward.addAwards(monsterKillAward);
			}
			int extrRandomAward377 = player.getEffect().getEffVal(EffType.MONSTER_EXTR_AWARD_HERO, getMarchEntity().getEffectParams());
			if (HawkRand.randInt(10000) < extrRandomAward377) {
				int effect377LinkToAwardId = ConstProperty.getInstance().getEffect377LinkToAwardId();
				if (effect377LinkToAwardId != 0) {
					killAward.addAward(effect377LinkToAwardId);
				}
			}
			// 指挥官经验
			double exp500 = player.getEffect().getEffVal(EffType.PLAYER_EXP_PER, getMarchEntity().getEffectParams()) * GsConst.EFF_PER;
			double exp515 = player.getEffect().getEffVal(EffType.PLAYER_EXP_PER_MONSTER, getMarchEntity().getEffectParams()) * GsConst.EFF_PER;
			double exp337 = player.getEffect().getEffVal(EffType.EFF_337) * GsConst.EFF_PER;
			int addExp = (int)(monsterCfg.getCommanderExp() * (1 + exp500 + exp515 + exp337));
			killAward.appendAward(AwardItems.valueOf().addExp(addExp));
			
			// eff:4022
			int buff = i > 0 ? 0 : player.getEffect().getEffVal(EffType.KILL_MONSTER_AWARD_ADD, getMarchEntity().getEffectParams());
			for (ItemInfo award : killAward.getAwardItems()) {
				int count = (int)(award.getCount() * (1 + buff * GsConst.EFF_PER));
				award.setCount(count);
			}
			// 能量探测器翻倍
			long buffEndTime = player.getData().getBuffEndTime(EffType.ATK_MONSTER_ENERGY_DETECTOR_VALUE);
			if (buffEndTime > now) {
				for (ItemInfo award : killAward.getAwardItems()) {
					boolean isEnergyDetectorTool = WorldMarchConstProperty.getInstance().isEnergyDetectorTool(award.getItemId());
					if (award.getItemType() != ItemType.TOOL || !isEnergyDetectorTool) {
						continue;
					}
					int energyDetectorMultipleEffect = WorldMarchConstProperty.getInstance().getEnergyDetectorMultipleEffect();
					award.setCount(award.getCount() * energyDetectorMultipleEffect);
				}
			}
			//回归特权 eff:28102   奖励翻倍,多添加一次原始奖励和经验
			int addVal =  player.getEffect().getEffVal(EffType.BACK_PRIVILEGE_ATK_MONSTER_AWARD_DOUBLE_TIMES);
			if(addVal > 0){
				//只有第一次需要从redis中拉取数据，后面都用缓存
				if (effTimes < 0) {
					effTimes = RedisProxy.getInstance().effectTodayUsedTimes(player.getId(), EffType.BACK_PRIVILEGE_ATK_MONSTER_AWARD_DOUBLE_TIMES);
				}
				if(effTimes < addVal){
					killAward.appendAward(AwardItems.valueOf().addExp(monsterCfg.getCommanderExp())); //指挥官经验
					killAward.addAwards(monsterKillAward); //道具
					addTimes++; //相当于更新了一次redis数据
					effTimes++; //redis数据更新了，缓存数据也得更新
				}
			}
			
			//添加限制道具获取记录
			for(ItemInfo info : killAward.getAwardItems()){
				boolean limit = ConstProperty.getInstance().checkBlazeMedalLimitItem(info.getItemId());
				if(limit){
					monsterEntity.addDropLimit(info.getItemId(), (int)info.getCount());
				}
			}
		} //for循环结尾
		
		AwardItems killAward = AwardItems.valueOf();
		for (AwardItems award : killAwardList) {
			killAward.appendAward(award);
		}
		
		//将前面累计的redis更新次数，一次性更新到redis中
		if (addTimes > 0) {
			RedisProxy.getInstance().effectTodayUseInc(player.getId(), EffType.BACK_PRIVILEGE_ATK_MONSTER_AWARD_DOUBLE_TIMES, addTimes);
		}
		
		for (ItemInfo award : killAward.getAwardItems()) {
			boolean isEnergyDetectorTool = WorldMarchConstProperty.getInstance().isEnergyDetectorTool(award.getItemId());
			if (award.getItemType() == ItemType.TOOL) {
				if(isEnergyDetectorTool){
					ActivityManager.getInstance().postEvent(new PowerLabItemDropEvent(player.getId(), award.getItemId(), (int) award.getCount())); //8 -- 合并count数量即可
				}
				if(award.getItemId() == ActivityConst.HONOR_ITEMID){
					ActivityManager.getInstance().postEvent(new HonorItemDropEvent(player.getId(), (int) award.getCount()));  //9 -- 合并count数量即可
				}
			}
		}
		
		// 首杀奖励
		AwardItems firstKillAward = AwardItems.valueOf();
		if (monsterCfg.getLevel() > killedLvl && monsterCfg.getFirstKillaward() != 0) {
			firstKillAward.addAward(monsterCfg.getFirstKillaward());
		}
		// 发邮件
		MonsterMail.Builder mailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, heroId, monsterCfg.getId(), point, 
				killAward.getAwardItems(), firstKillAward.getAwardItems(), 0, 0, 0, atkTimes);
		YuriMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.MONSTER_SUCC)
				.addContents(mailBuilder).addTips(monsterCfg.getId()).build()); //10 -- 需要策划提供新的邮件模板
		// 投递发奖
		player.dealMsg(MsgId.ATTACK_MONSTER_AWARD, new MonsterAtkAwardMsgInvoker(player, monsterCfg.getId(), killAward, firstKillAward, AwardItems.valueOf(), atkTimes)); //11 -- 直接合并奖励即可
		//同步击杀数量
		player.getPush().syncMonsterKillData();
	}
	
}