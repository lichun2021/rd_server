package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.WorldNewMonsterDamageUp;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.guild.guildrank.GuildRankMgr;
import com.hawk.game.invoker.MonsterAtkAwardMsgInvoker;
import com.hawk.game.invoker.NewMonsterVitReturnInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Mail.MonsterMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventAttackNewMonster;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.service.WorldMonsterService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 新版野怪行军
 * @author golden
 *
 */
public class NewMonsterMarch extends PlayerMarch implements BasedMarch {

	private static Logger logger = LoggerFactory.getLogger("Server");
	
	public NewMonsterMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.NEW_MONSTER;
	}

	@Override
	public void onMarchReach(Player player) {
		// 行军
		WorldMarch march = getMarchEntity();
		// 目标点
		int terminalId = march.getTerminalId();
		// 目标野怪
		int monsterId = Integer.valueOf(march.getTargetId());
		// 点和怪信息
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(terminalId);
		
		if (point == null || point.getPointType() != WorldPointType.MONSTER_VALUE || point.getMonsterId() != monsterId) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.WORLD_NEW_MONSTER_POINT_CHANGED)
					.addTips(monsterId)
					.build());
			// 体力返还
			reurnVit(monsterId, march.getAttackTimes());
			WorldMarchService.getInstance().onMarchReturn(this, march.getArmys(), 0);
			logger.info("new monster march reach, point changed, marchId:{}", this.getMarchId());
			return;
		}
		
		// 野怪配置
		WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
		
		/**********************    战斗数据组装及战斗***************************/
		// 进攻方玩儿家
		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(player);

		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);
		
		// 猎杀加成次数
		int additionTimes = LocalRedis.getInstance().getAtkNewMonsterTimes(player.getId(), point.getId());
		int hurtBuff = getHurtBuff(additionTimes);
		PveBattleIncome battleIncome = BattleService.getInstance().initMonsterBattleData(BattleConst.BattleType.ATTACK_NEW_MONSTER, point.getId(), monsterCfg.getId(), atkPlayers, atkMarchs, GsConst.RANDOM_MYRIABIT_BASE, hurtBuff);
		
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		/**********************    战斗数据组装及战斗***************************/
		
		// 战斗结果处理
		doBattleResult(player, point, monsterCfg, battleOutcome, march.getAttackTimes(), additionTimes, march.getHeroIdList());
		// 发送战斗结果
		WorldMarchService.getInstance().sendBattleResultInfo(this, battleOutcome.isAtkWin(), WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk()), new ArrayList<ArmyInfo>(), point.getRemainBlood() <= 0);
		// pve事件
		postEventAfterPve(player, battleOutcome, getMarchType(), monsterCfg.getLevel());
		// 行军返回
		WorldMarchService.getInstance().onMarchReturn(this, battleOutcome.getAftArmyMapAtk().get(this.getMarchEntity().getPlayerId()), 0);
		// 战斗胜利，移除点
		if (point.getRemainBlood() <= 0) {
			// 移除野怪点
			WorldPointService.getInstance().removeWorldPoint(terminalId);
		} else {
			WorldPointService.getInstance().getWorldScene().update(point.getAoiObjId());
		}
		
		// 刷新战力
//		refreshPowerAfterWar(atkPlayers, null);
	}
	
	/**
	 * 战斗结果处理
	 * @param leader
	 * @param point
	 * @param monsterCfg
	 * @param atkPlayers
	 * @param isAtkWin
	 */
	private void doBattleResult(Player player, WorldPoint point, WorldEnemyCfg monsterCfg, BattleOutcome battleOutcome, int atkTimes, int atkNewMonsterTimes, List<Integer> heroIds) {
		int maxEnemyBlood = WorldMonsterService.getInstance().getMaxEnemyBlood(monsterCfg.getId());
		// 攻击前血量
		int before = point.getRemainBlood();
		// 实际攻击次数
		int pracAtkTimes = getPracAtkTimes(battleOutcome, atkTimes, before);
		// 实际击杀
		int pracKill = getOnceKillMonsterCount(battleOutcome) * pracAtkTimes;
		pracKill = pracKill > maxEnemyBlood ? maxEnemyBlood : pracKill;
		// 攻击后血量
		int after = (before - pracKill < 0) ? 0 : (before - pracKill);
		
		// 奖励
		doAward(player, point, monsterCfg, battleOutcome, pracKill, after, atkNewMonsterTimes, atkTimes, pracAtkTimes);
		// 设置点剩余血量
		point.setRemainBlood(after);
		boolean isKill = after == 0;
		
		// 设置最大击杀野怪等级
		boolean isFirstKill = false;
		int newMonsterKileLvl = player.getData().getMonsterEntity().getNewMonsterKileLvl();
		if (isKill && monsterCfg.getLevel() > newMonsterKileLvl) {
			player.getData().getMonsterEntity().setNewMonsterKileLvl(monsterCfg.getLevel());
			isFirstKill = true;
		}
		
		// 设置猎杀加成
		LocalRedis.getInstance().updateAtkNewMonsterInfo(player.getId(), point.getId(), atkNewMonsterTimes + 1);
		// 同步野怪击杀等级
		player.getPush().syncMonsterKilled(monsterCfg.getId(), isKill);
		MonsterAttackEvent event = new MonsterAttackEvent(player.getId(), 
				monsterCfg.getType(), monsterCfg.getId(), monsterCfg.getLevel(), pracAtkTimes, isKill, !heroIds.isEmpty());
		// 活动事件
		ActivityManager.getInstance().postEvent(event);
		// 跨服消息投递-野怪击杀
		CrossActivityService.getInstance().postEvent(event);
		// 任务事件
		MissionManager.getInstance().postMsg(player, new EventAttackNewMonster(monsterCfg.getId(), monsterCfg.getLevel(), atkTimes, pracAtkTimes, isKill));
		// 体力返还
		if (atkTimes > pracAtkTimes) {
			reurnVit(monsterCfg.getId(), atkTimes - pracAtkTimes);
		}
		
		// 日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.ATK_NEW_MONSTER,
				Params.valueOf("x", point.getX()),
				Params.valueOf("y", point.getY()),
				Params.valueOf("monsterId", monsterCfg.getId()),
				Params.valueOf("monsterLvl", monsterCfg.getLevel()),
				Params.valueOf("atkTimes", atkTimes),
				Params.valueOf("pracAtkTimes", pracAtkTimes),		
				Params.valueOf("beforeBlood", before),
				Params.valueOf("afterBlood", after),
				Params.valueOf("isKill", isKill));
		
		LogUtil.logAttackMonster(player, point.getX(), point.getY(), monsterCfg.getType(), 
				monsterCfg.getId(), monsterCfg.getLevel(), atkTimes, pracAtkTimes, before, after, isKill, isFirstKill, true);
		
		GuildRankMgr.getInstance().onPlayerKillMonster(player.getId(), player.getGuildId(),1 );
	}

	/**
	 * 战斗奖励
	 * @param player
	 * @param point 点
	 * @param monsterCfg 野怪配置
	 * @param battleOutcome 战斗输出
	 * @param totalKill 总击杀
	 * @param after 攻击后血量
	 * @param atkNewMonsterTimes 第n次攻击
	 * @param atkTimes 连续攻击次数
	 * @param pracAtkTimes 实际攻击次数
	 */
	private void doAward(Player player, WorldPoint point, WorldEnemyCfg monsterCfg, BattleOutcome battleOutcome, int totalKill, int after, int atkNewMonsterTimes, int atkTimes, int pracAtkTimes) {
		AwardItems atkAward = AwardItems.valueOf();
		AwardItems damageAward = AwardItems.valueOf();
		AwardItems firstKillAward = AwardItems.valueOf();

		for (int i = 0; i < pracAtkTimes; i++) {
			// 攻击奖励
			atkAward.addAwards(monsterCfg.getAttackAwards());
			
			// 攻击额外奖励 -- 375
			int extrAward375 = player.getEffect().getEffVal(EffType.NEW_MONSTER_EXTR_AWARD, getMarchEntity().getEffectParams()) / 10000;
			for (int j = 0; j < extrAward375; j++) {
				atkAward.addAwards(monsterCfg.getAttackAwards());
			}
			
			// 攻击额外随机奖励 -- 375
			int extrRandomAward375 = player.getEffect().getEffVal(EffType.NEW_MONSTER_EXTR_AWARD, getMarchEntity().getEffectParams()) % 10000;
			if (HawkRand.randInt(10000) < extrRandomAward375) {
				atkAward.addAwards(monsterCfg.getAttackAwards());
			}
			
			// 攻击额外奖励 -- 376
			int extrAward376 = player.getEffect().getEffVal(EffType.MONSTER_EXTR_AWARD, getMarchEntity().getEffectParams()) / 10000;
			for (int k = 0; k < extrAward376; k++) {
				atkAward.addAwards(monsterCfg.getAttackAwards());
			}

			// 攻击额外随机奖励 -- 376
			int extrRandomAward376 = player.getEffect().getEffVal(EffType.MONSTER_EXTR_AWARD, getMarchEntity().getEffectParams()) % 10000;
			if (HawkRand.randInt(10000) < extrRandomAward376) {
				atkAward.addAwards(monsterCfg.getAttackAwards());
			}
			
			// 指挥官经验
			int addExp = monsterCfg.getCommanderExp();
			double exp513 = player.getEffect().getEffVal(EffType.NEW_MONSTER_EXP_ADD, getMarchEntity().getEffectParams()) * GsConst.EFF_PER;
			double exp515 = player.getEffect().getEffVal(EffType.PLAYER_EXP_PER_MONSTER, getMarchEntity().getEffectParams()) * GsConst.EFF_PER;
			double exp337 = player.getEffect().getEffVal(EffType.EFF_337) * GsConst.EFF_PER;
			addExp *= 1 + exp513 + exp515 + exp337;
			atkAward.appendAward(AwardItems.valueOf().addExp(addExp));
		}
		
		// 伤害奖励
		float killPercent = (float)totalKill / (float)pracAtkTimes /(float)WorldMonsterService.getInstance().getMaxEnemyBlood(monsterCfg.getId());
		int damageAwardRate = (int)(killPercent * GsConst.RANDOM_MYRIABIT_BASE * GsConst.RANDOM_MYRIABIT_BASE / monsterCfg.getDamageAwardCoefficient());
		for (int i = 0; i < pracAtkTimes; i++) {
			if (HawkRand.randInt(10000) > damageAwardRate) {
				continue;
			}
			damageAward.addAwards(monsterCfg.getDamageAwards());
		}

		// 首杀奖励
		int newMonsterKileLvl = player.getData().getMonsterEntity().getNewMonsterKileLvl();
		if (after == 0 && monsterCfg.getLevel() > newMonsterKileLvl) {
			firstKillAward.addAward(monsterCfg.getFirstKillaward());
		}
		
		// 联盟奖励邮件
		if (after == 0) {
			sendKillGuildAwardMail(player, point, monsterCfg, battleOutcome);
		}
		
		// 发野怪战斗邮件
		float maxEnemyBlood = WorldMonsterService.getInstance().getMaxEnemyBlood(monsterCfg.getId());
		MonsterMail.Builder mailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, this.getMarchEntity().getHeroIdList(),
				monsterCfg.getId(), point, atkAward.getAwardItems(), firstKillAward.getAwardItems(), (float)after / maxEnemyBlood, (float)totalKill / maxEnemyBlood, 0);
		mailBuilder.setAtkTimes(atkNewMonsterTimes + 1);
		mailBuilder.setAtkBuff(getHurtBuff(atkNewMonsterTimes));
		mailBuilder.setAtkCount(atkTimes);
		mailBuilder.setValidCount(pracAtkTimes);
		mailBuilder.setAtkValue((float)totalKill / maxEnemyBlood);
		if (damageAward != null) {
			damageAward.getAwardItems().forEach(award -> mailBuilder.addRewards(award.toRewardItem()));
		}
		
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.WORLD_NEW_MONSTER_ATK_REPORT)
				.addContents(mailBuilder)
				.addTips(monsterCfg.getId())
 				.build());
		
		// 投递发奖
		player.dealMsg(MsgId.ATTACK_MONSTER_AWARD, new MonsterAtkAwardMsgInvoker(player, monsterCfg.getId(), atkAward, firstKillAward, damageAward, pracAtkTimes));
	}

	/**
	 * 获取总击杀数量
	 * @param battleOutcome
	 * @param atkTimes
	 * @param before
	 * @return
	 */
	private int getPracAtkTimes(BattleOutcome battleOutcome, int atkTimes, int before) {
		int kill = getOnceKillMonsterCount(battleOutcome);
		int pracAtkTimes = 0;
		for (int i = 0; i < atkTimes; i++) {
			pracAtkTimes++;
			if (before - (kill * pracAtkTimes) <= 0) {
				break;
			}
		}
		return pracAtkTimes;
	}
	
	/**
	 * 获取单次击杀量
	 * @param battleOutcome
	 * @return
	 */
	private int getOnceKillMonsterCount(BattleOutcome battleOutcome) {
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
	 * 发邮件：联盟击杀奖励邮件
	 * @param leader
	 * @param point
	 * @param monsterCfg
	 * @param battleOutcome
	 */
	private void sendKillGuildAwardMail(Player leader, WorldPoint point, WorldEnemyCfg monsterCfg, BattleOutcome battleOutcome) {
		// 联盟奖励
		// 联盟成员发放礼物
		int allianceGift = monsterCfg.getAllianceGift();
		if (!leader.isCsPlayer() && leader.hasGuild() && allianceGift > 0) {
			GuildService.getInstance().bigGift(leader.getGuildId()).addSmailGift(allianceGift, false);
		}
	}
	
	/**
	 * 获取猎杀加成
	 * @param additionTimes
	 * @return
	 */
	private int getHurtBuff(int additionTimes) {
		if (additionTimes == 0) {
			return 0;
		}

		int configSize = HawkConfigManager.getInstance().getConfigSize(WorldNewMonsterDamageUp.class);
		additionTimes = (additionTimes > configSize) ? configSize : additionTimes;

		WorldNewMonsterDamageUp cfg = HawkConfigManager.getInstance().getConfigByIndex(WorldNewMonsterDamageUp.class, additionTimes - 1);
		int buffValue = cfg.getDamageBonus();
		return buffValue;
	}
	
	/**
	 * 返还体力
	 */
	private void reurnVit(int monsterId, int atkTimes) {
		int physicalPowerReturnCoe = ConstProperty.getInstance().getPhysicalPowerReturnCoe();
		int returnVit = (int)Math.ceil(this.getMarchEntity().getVitCost() * atkTimes * physicalPowerReturnCoe * GsConst.EFF_PER);
		getPlayer().dealMsg(MsgId.RETURN_VIT, new NewMonsterVitReturnInvoker(getPlayer(), returnVit));
	}
}
