package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.CityOnFireMsgInvoker;
import com.hawk.game.invoker.PlayerArmyBackMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.skill.talent.ITalentSkill;
import com.hawk.game.player.skill.talent.Skill10104;
import com.hawk.game.player.skill.talent.TalentSkillContext;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.GuildWar.GuildWarMarchType;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.PushService;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventAttackPlayerCity;
import com.hawk.game.service.mssion.event.EventGrabResource;
import com.hawk.game.util.AlgorithmPoint;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.GsConst.MissionFunType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.DefenderIdentity;
import com.hawk.log.Source;

/**
 * 攻击单人
 * 
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class AttackPlayerMarch extends PassiveMarch implements BasedMarch, IReportPushMarch {
	public static String DUEL_MARK = "10000_1007_1";
	private BattleSkillType skillType;
	private boolean isDuel; //是决斗
	public AttackPlayerMarch(WorldMarch marchEntity) {
		super(marchEntity);
		isDuel = Objects.equals(getAssistantStr(), DUEL_MARK);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.ATTACK_PLAYER;
	}

	@Override
	public void onMarchReach(Player atkPlayer) {
		// 删除行军报告
		removeAttackReport();
		// 行军
		WorldMarch atkMarch = getMarchEntity();
		// 目标点
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(atkMarch.getTerminalId());
		// 防守玩家
		Player defPlayer = GlobalData.getInstance().makesurePlayer(atkMarch.getTargetId());

		// 防守玩家处于预跨服状态，不处理
		if (defPlayer == null) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			return;
		}
		
		// 战斗前检查条件
		if (!this.checkPVPBeforeWar(targetPoint, defPlayer, atkPlayer)) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			return;
		}
		
		if (isDuel) {
			ITalentSkill talentSkill = TalentSkillContext.getInstance().getSkill(GsConst.SKILL_10104);
			boolean canDuel = talentSkill.touchSkill(atkPlayer, defPlayer.getId(), ArmyService.getInstance().getArmysCount(atkMarch.getArmys()));
			if (!canDuel) {
				WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
				return;
			}
		}

		/********************** 战斗数据组装及战斗 ***************************/
		String attackStr = "[marchId:" + atkMarch.getMarchId() + ",army:" + atkMarch.getArmyStr() + "]";
		// 进攻方玩家
		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(atkPlayer);

		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		defPlayers.add(defPlayer);
		// 防守方援军
		Set<IWorldMarch> helpMarchList = new HashSet<>();
		
		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);

		// 防守方行军
		List<IWorldMarch> defMarchs = new ArrayList<>();

		// 判断触发技能
		skillType = getTouchSkillType(atkPlayer, defPlayer, atkMarch);
		if (skillType.equals(BattleSkillType.BATTLE_SKILL_DUEL)) {
			atkPlayer.removeSkillBuff(GsConst.SKILL_10104);// 立即CD. 防止战斗失败buff移不掉了
		} else {
			// 防守方援军
			helpMarchList = getDefMarch4War(defPlayer, defPlayers);
			for (IWorldMarch iWorldMarch : helpMarchList) {
				defMarchs.add(iWorldMarch);
			}
		}

		// 战斗数据输入
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.ATTACK_CITY,
				targetPoint.getId(), atkPlayers, defPlayers, atkMarchs, defMarchs, skillType);
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);

		// 战斗胜利
		final boolean isAtkWin = battleOutcome.isAtkWin();

		/********************** 战斗数据组装及战斗 ***************************/

		// 攻击方剩余兵力
		Map<String, List<ArmyInfo>> atkArmyLeftMap = battleOutcome.getAftArmyMapAtk();
		List<ArmyInfo> atkArmyLeft = atkArmyLeftMap.get(atkPlayer.getId());

		// 防守方剩余兵力
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();
		List<ArmyInfo> defArmyList = WorldUtil.mergAllPlayerArmy(defArmyLeftMap);

		// 准备扣资源、加资源
		final AwardItems awardItems = AwardItems.valueOf();
		final ConsumeItems consumeItems = ConsumeItems.valueOf();
		Map<String, long[]> grabResMap = null;
		// 掠夺资源模块是否关闭
		if (!SystemControler.getInstance().isSystemItemsClosed(ControlerModule.GRABRESENABLE)) {
			int grabWeight = isAtkWin ? WorldUtil.calcTotalWeight(atkPlayer, atkArmyLeft, atkMarch.getEffectParams()) : 0;
			long[] hasResAry = defPlayer.getPlunderResAry(GsConst.RES_TYPE);
			grabResMap = BattleService.getInstance().calcGrabRes(atkPlayers, new int[] { grabWeight }, hasResAry);

			// 资源变动: 这里资源保护的情况下可能为null
			if (hasResAry != null && grabResMap != null) {
				long[] grabsResArr = grabResMap.get(atkPlayer.getId());
				for (int i = 0; i < GsConst.RES_TYPE.length; i++) {
					int resCount = (int) grabsResArr[i];
					if (resCount > 0) {
						awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, GsConst.RES_TYPE[i], resCount);
						consumeItems.addConsumeInfo(PlayerAttr.valueOf(GsConst.RES_TYPE[i]), resCount);
						MissionManager.getInstance().postMsg(atkPlayer,
								new EventGrabResource(GsConst.RES_TYPE[i], resCount));

					}
				}
			}
		} else {
			List<String> atkPlayerIds = new ArrayList<>();
			for (Player aPlayer : atkPlayers) {
				atkPlayerIds.add(aPlayer.getId());
			}

			List<String> defPlayerIds = new ArrayList<>();
			for (Player dPlayer : defPlayers) {
				defPlayerIds.add(dPlayer.getId());
			}

			WorldMarchService.logger.error("grab res model closed, atkPlayers:{}, defPlayers:{}",
					atkPlayerIds.toString(), defPlayerIds.toString());
		}

		// 行为日志
		BehaviorLogger.log4Service(atkPlayer, Source.WORLD_ACTION, Action.WORLD_MARCH_REACH_ATTACK_PLAYER,
				Params.valueOf("marchData", atkMarch), Params.valueOf("attackArmyBefore", attackStr),
				Params.valueOf("isAtkWin", isAtkWin),
				Params.valueOf("atkLeftArmyList", WorldUtil.mergAllPlayerArmy(atkArmyLeftMap)),
				Params.valueOf("defLeftArmyList", defArmyList));

		// 发送战斗结果给前台播放动画
		WorldMarchService.getInstance().sendBattleResultInfo(this, isAtkWin, atkArmyLeft, defArmyList, isAtkWin);

		// 保存联盟战争信息
		LocalRedis.getInstance().saveGuildBattleInfo(atkPlayers, defPlayers, isAtkWin, GuildWarMarchType.SINGLE_TYPE,
				helpMarchList.isEmpty() ? GuildWarMarchType.SINGLE_TYPE : GuildWarMarchType.MASS_TYPE);
		GuildService.getInstance().pushNewGuildWarRecordCount(atkPlayer.getGuildId(), defPlayer.getGuildId());

		// 记录玩家战斗安全日志
		LogUtil.logSecBattleFlow(atkPlayer, defPlayer, "", DefenderIdentity.CITY, isAtkWin, awardItems, consumeItems,
				atkArmyLeft, defArmyList, 0, atkMarch, false);

		// 敌方大本等级
		int constrFactorLvl = defPlayer.getCityLevel();

		// 战后统计
		sendMsgUpdateAtkPlayerAfterWar(isAtkWin, atkArmyLeft, constrFactorLvl, atkPlayer, battleOutcome);
		if (isAtkWin) {
			MissionService.getInstance().missionRefresh(atkPlayer, MissionFunType.FUN_CITYATK_BATTLE_WIN, 0, 1);
		} else {
			MissionService.getInstance().missionRefresh(defPlayer, MissionFunType.FUN_CITYDEF_BATTLE_WIN, 0, 1);
		}

		sendMsgUpdateDefPlayerListAfterWar(defPlayers, battleOutcome, consumeItems);

		// 防御者战后部队结算
		defPlayer.dealMsg(MsgId.ARMY_BACK, new PlayerArmyBackMsgInvoker(targetPoint.getPointType(), defPlayer,
				battleOutcome, battleIncome, grabResMap, true, isMassMarch(), this.getMarchType()));
		// 攻方行军返回
		WorldMarchService.getInstance().onMarchReturn(this, awardItems, atkArmyLeft, 0);

		// 更新援助防御玩家行军的部队
		List<IWorldMarch> helpMarchs = new ArrayList<IWorldMarch>();
		for (IWorldMarch tempMarch : helpMarchList) {
			helpMarchs.add(tempMarch);
		}
		updateDefMarchAfterWar(helpMarchs, defArmyLeftMap);

		// 防守方失败了 , 城墙着火
		if (isAtkWin) {
			// 投递回玩家线程执行
			defPlayer.dealMsg(MsgId.CITY_ON_FIRE,
					new CityOnFireMsgInvoker(atkPlayer, defPlayer, atkMarch.getHeroIdList()));
		}

		// 移除技能buff
		if (skillType.equals(BattleSkillType.BATTLE_SKILL_LIFESAVING)) {
			atkPlayer.removeSkillBuff(GsConst.SKILL_10103);
		} else if (skillType.equals(BattleSkillType.BATTLE_SKILL_DUEL)) {
			RedisProxy.getInstance().incPlayerDueled(defPlayer.getId());
		}

		MissionManager.getInstance().postMsg(atkPlayer, new EventAttackPlayerCity(isAtkWin));

		// 刷新战力
		refreshPowerAfterWar(atkPlayers, defPlayers);
	}

	@Override
	public void onWorldMarchReturn(Player player) {
		WorldMarch march = getMarchEntity();
		String awardStr = march.getAwardStr();
		if (!HawkOSOperator.isEmptyString(awardStr)) {
			AwardItems award = AwardItems.valueOf(awardStr);
			award.rewardTakeAffectAndPush(player, Action.WORLD_MARCH_ATTACK_PLAYER_RETURN);
		}

		// 进攻部队返回推送
		String enemyName = GlobalData.getInstance().getPlayerNameById(march.getTargetId());
		PushService.getInstance().pushMsg(player.getId(), PushMsgType.ACTTACK_ARMY_RETURN_VALUE, enemyName);

		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_ATTACK_PLAYER_RETURN,
				Params.valueOf("march", march), Params.valueOf("awardStr", awardStr));

	}

	/**
	 * 检查PVP战斗是否满足前置条件，不满足则发邮件通知，现在普通攻击大本，集结攻击大本
	 * 
	 * @param march
	 * @param tarPoint
	 * @param defPlayer
	 * @param isMass
	 *            ： 是否是集结
	 * @return
	 */
	@Override
	public boolean checkPVPBeforeWar(WorldPoint tarPoint, Player defPlayer, Player atkPlayer) {

		// 行军信息
		WorldMarch march = getMarchEntity();
		int x = march.getTerminalX();
		int y = march.getTerminalY();
		String targetId = march.getTargetId();

		int atkIcon = GuildService.getInstance().getGuildFlagByPlayerId(march.getPlayerId());
		String atkName = GameUtil.getPlayerNameWithGuildTag(defPlayer.getGuildId(), defPlayer.getName());

		// 找不到目标: 攻击方发送邮件 -> 攻击玩家基地失败，被攻击方高迁或被打飞
		if (tarPoint == null) {

			FightMailService.getInstance()
					.sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId())
							.setMailId(MailId.ATTACK_BASE_FAILED_TARGET_MOVED).addSubTitles(x, y)
							.addContents(atkName, x, y).setIcon(atkIcon).build());

			WorldMarchService.logger.info("attack player march check, point null, x:{}, y{}, targetId:{}", x, y,
					targetId);
			return false;
		}

		// 目标非玩家，或者目标已换人，或者目标和自己同盟不能再打: 攻击方发送邮件 -> 攻击玩家基地失败，被攻击方状态改变
		if (tarPoint.getPointType() != WorldPointType.PLAYER_VALUE || !tarPoint.getPlayerId().equals(targetId)
				|| GuildService.getInstance().isInTheSameGuild(atkPlayer.getId(), defPlayer.getId())) {

			FightMailService.getInstance()
					.sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId())
							.setMailId(MailId.ATTACK_BASE_FAILED_TARGET_CHANGED).addSubTitles(x, y)
							.addContents(atkName, x, y).setIcon(atkIcon).build());

			WorldMarchService.logger.info("attack player march check, point changed, x:{}, y{}, targetId:{}", x, y,
					targetId);
			return false;
		}
		
		long cityShieldTime = defPlayer.getData().getCityShieldTime();
		boolean shieldNotEnd = cityShieldTime > HawkTime.getMillisecond();
		if (shieldNotEnd && tarPoint.getShowProtectedEndTime() < cityShieldTime) {
			tarPoint.setProtectedEndTime(cityShieldTime);
			// 通知场景本点数据更新
			WorldPointService.getInstance().getWorldScene().update(tarPoint.getAoiObjId());
		}

		// 防守玩家开启保护罩
		if (shieldNotEnd || HawkTime.getMillisecond() <= tarPoint.getShowProtectedEndTime()) {

			// 发送邮件---攻击玩家基地失败，被攻击方状态改变（攻击方）
			int defIcon = GuildService.getInstance().getGuildFlagByPlayerId(defPlayer.getId());
			String defName = GameUtil.getPlayerNameWithGuildTag(defPlayer.getGuildId(), defPlayer.getName());
			FightMailService.getInstance()
					.sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId())
							.setMailId(MailId.ATTACK_BASE_FAILED_PROTECTION_TO_FROM).addSubTitles(x, y)
							.addContents(defName, x, y).setIcon(defIcon).build());

			// 发送邮件---攻击玩家基地失败，被攻击方开启保护（被攻击方）
			int[] pos = WorldPlayerService.getInstance().getPlayerPosXY(atkPlayer.getId());
			FightMailService.getInstance()
					.sendMail(MailParames.newBuilder().setPlayerId(defPlayer.getId())
							.setMailId(MailId.ATTACK_BASE_FAILED_PROTECTION_TO_TARGET).addContents(
									GameUtil.getPlayerNameWithGuildTag(atkPlayer.getGuildId(), atkPlayer.getName()),
									pos[0], pos[1])
							.setIcon(atkIcon).build());

			WorldMarchService.logger.info("attack player march check, point protected, x:{}, y{}, targetId:{}", x, y,
					targetId);
			return false;
		}

		return true;
	}

	/**
	 * 触发技能类型
	 * 
	 * @param atkPlayer攻击方玩家
	 * @param defPlayer防守方玩家
	 * @param atkMarch攻击方行军
	 * @return
	 */
	private BattleSkillType getTouchSkillType(Player atkPlayer, Player defPlayer, WorldMarch atkMarch) {
		// 触发技能判断: 救援
		BattleSkillType skillType = BattleSkillType.BATTLE_SKILL_NONE;
		ITalentSkill talentSkill = TalentSkillContext.getInstance().getSkill(GsConst.SKILL_10103);
		if (talentSkill.touchSkill(atkPlayer, null)) {
			skillType = BattleSkillType.BATTLE_SKILL_LIFESAVING;
			WorldMarchService.logger.info("touchTalentSkill lifesaving, playerId:{}, marchId:{}", atkPlayer.getId(), atkMarch.getMarchId());
		}
		// 触发技能判断: 决斗
		talentSkill = TalentSkillContext.getInstance().getSkill(GsConst.SKILL_10104);
		if (isDuel
				&& talentSkill.touchSkill(atkPlayer, defPlayer.getId(), ArmyService.getInstance().getArmysCount(atkMarch.getArmys()))) {
			skillType = BattleSkillType.BATTLE_SKILL_DUEL;
			WorldMarchService.logger.info("touchTalentSkill duel, playerId:{}, marchId:{}", atkPlayer.getId(), atkMarch.getMarchId());
		}
		return skillType;
	}

	@Override
	public boolean needShowInGuildWar() {
		return true;
	}

	@Override
	public void onMarchStart() {
		this.pushAttackReport();
	}

	@Override
	public void onMarchReturn() {
		// 删除行军报告
		this.removeAttackReport();
	}

	@Override
	public void remove() {
		if (BattleSkillType.BATTLE_SKILL_DUEL != skillType && isDuel) { // 如果没有发生决斗 且是决斗目标
			Skill10104 talentSkill = TalentSkillContext.getInstance().getSkill(GsConst.SKILL_10104);
			talentSkill.forceCoolDown(getPlayer());
		}
		super.remove();
		// 删除行军报告
		this.removeAttackReport();
	}

	@Override
	public Set<String> attackReportRecipients() {
		return ReportRecipients.TargetAndHisAssistance.attackReportRecipients(this);
	}

	@Override
	public void targetMoveCityProcess(Player targetPlayer, long currentTime) {
		if (isMarchState()) {
			AlgorithmPoint point = WorldUtil.getMarchCurrentPosition(this.getMarchEntity());
			WorldMarchService.getInstance().onMarchReturn(this, currentTime, this.getMarchEntity().getAwardItems(),
					this.getMarchEntity().getArmys(), point.getX(), point.getY());
		}
		WorldMarchService.getInstance().rmGuildMarch(this.getMarchId());
	}

	public boolean beforeImmediatelyRemoveMarchProcess(Player player) {
		WorldMarch march = getMarchEntity();
		String awardStr = march.getAwardStr();
		if (!HawkOSOperator.isEmptyString(awardStr)) {
			AwardItems award = AwardItems.valueOf(awardStr);
			award.rewardTakeAffectAndPush(player, Action.WORLD_MARCH_ATTACK_PLAYER_RETURN);
		}

		// 进攻部队返回推送
		String enemyName = GlobalData.getInstance().getPlayerNameById(march.getTargetId());
		PushService.getInstance().pushMsg(player.getId(), PushMsgType.ACTTACK_ARMY_RETURN_VALUE, enemyName);

		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_ATTACK_PLAYER_RETURN,
				Params.valueOf("march", march), Params.valueOf("awardStr", awardStr));
		return true;
	}

}
