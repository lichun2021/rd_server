package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.skill.talent.ITalentSkill;
import com.hawk.game.player.skill.talent.TalentSkillContext;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GameUtil;
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
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.object.MapBlock;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;
import com.hawk.log.LogConst.DefenderIdentity;
import com.hawk.log.Source;

/**
 * 驻扎行军
 * 
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class ArmyQuarteredMarch extends PlayerMarch implements BasedMarch, IReportPushMarch, IPassiveAlarmTriggerMarch {

	public ArmyQuarteredMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.ARMY_QUARTERED;
	}

	@Override
	public void onMarchReach(Player player) {
		marchReach(player);
		rePushPointReport();
	}

	private void marchReach(Player player) {
		// 行军
		WorldMarch march = getMarchEntity();

		// 不可驻扎
		if (!canQuarter(player, march)) {
			
			int pointId = march.getTerminalId();
			int[] pos = GameUtil.splitXAndY(pointId);
			
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			
			// 发送邮件
			FightMailService.getInstance().sendMail(MailParames
					.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.CAMP_FAILED_TARGET_CHANGED)
					.addSubTitles(pos[0], pos[1])
					.addContents(pos[0], pos[1])
					.build());
			return;
		}

		// 目标点
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());

		// 是否需要战斗
		boolean needFight = worldPoint == null ? false : true;
		if (!needFight) {
			int posX = march.getTerminalX();
			int posY = march.getTerminalY();
			quartered(player, worldPoint, march);
			
			// 查找朝向目标点行军的
			Collection<IWorldMarch> worldPointMarchs = WorldMarchService.getInstance().getWorldPointMarch(posX, posY);
			for (IWorldMarch iWorldMarch : worldPointMarchs) {
				iWorldMarch.updateMarch();
			}
			return;
		}

		// 敌方行军
		IWorldMarch defMarch = WorldMarchService.getInstance().getMarch(worldPoint.getMarchId());

		// 如果原有据点的行军为空，则直接走新的据点驻扎，移除有问题的据点，并记录日志
		if (defMarch == null) {
			quartered(player, worldPoint, march);
			return;
		}

		/************************** 战斗数据组装和战斗 *********************/
		Player defPlayer = GlobalData.getInstance().makesurePlayer(defMarch.getPlayerId());
		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(player);
		List<Player> defPlayers = new ArrayList<>();
		defPlayers.add(defPlayer);

		// 准备战斗双方
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);
		List<IWorldMarch> defMarchs = new ArrayList<>();
		defMarchs.add(defMarch);

		String selfArmyBefore = march.getArmyStr();
		String oppArmyBefore = defMarch.getMarchEntity().getArmyStr();

		// 触发技能判断: 救援
		BattleSkillType skillType = BattleSkillType.BATTLE_SKILL_NONE;
		ITalentSkill talentSkill = TalentSkillContext.getInstance().getSkill(GsConst.SKILL_10103);
		if (talentSkill.touchSkill(player, null)) {
			skillType = BattleSkillType.BATTLE_SKILL_LIFESAVING;
			WorldMarchService.logger.info("touchTalentSkill lifesaving, playerId:{}, marchId:{}", player.getId(), march.getMarchId());
		}

		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.ATTACK_QUARTERED, worldPoint.getId(), atkPlayers, defPlayers, atkMarchs, defMarchs,
				skillType);
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		/************************** 战斗数据组装和战斗 *********************/

		// 战斗结果
		boolean isWin = battleOutcome.isAtkWin();

		// 双方战后剩余部队
		Map<String, List<ArmyInfo>> atkArmyLeftMap = battleOutcome.getAftArmyMapAtk();
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();
		List<ArmyInfo> atkArmyLeft = atkArmyLeftMap.get(player.getId());
		List<ArmyInfo> defArmyLeft = defArmyLeftMap.get(defPlayer.getId());

		// 发送战斗结果，用于前端播放动画
		WorldMarchService.getInstance().sendBattleResultInfo(this, isWin, atkArmyLeft, defArmyLeft, isWin);

		// 发送战斗邮件
		FightMailService.getInstance().sendFightMail(worldPoint.getPointType(), battleIncome, battleOutcome, null);
		BattleService.getInstance().dealWithPvpBattleEvent(battleIncome, battleOutcome, false, this.getMarchType());
		// 记录战斗安全日志
		LogUtil.logSecBattleFlow(player, defPlayer, "", DefenderIdentity.QUARTER_FIELD, isWin, null, null, atkArmyLeft, defArmyLeft, 0, march, false);

		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_REACH_QUARTERED,
				Params.valueOf("isWin", isWin),
				Params.valueOf("myArmyBefore", selfArmyBefore),
				Params.valueOf("oppArmyBefore", oppArmyBefore),
				Params.valueOf("myArmyInfoList", atkArmyLeft),
				Params.valueOf("oppArmyInfoList", defArmyLeft),
				Params.valueOf("march", march),
				Params.valueOf("oppMarch", defMarch));

		// 战斗胜利
		if (isWin) {
			// 通知原来的驻扎者，后续的所有行军终止
			// WorldMarchService.getInstance().notifyTargetPlayerMarchEnd(defPlayer,
			// worldPoint);
			// 验证是否立即回家
			WorldMarchService.getInstance().onMarchReturn(defMarch, defArmyLeft, 0);
			// 胜利者驻扎
			this.onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE, atkArmyLeft, worldPoint);
			// 占领本世界点
			WorldPointService.getInstance().notifyPointOccupied(worldPoint.getId(), player, this, WorldPointType.QUARTERED);
			// 发送邮件---驻扎成功
			sendQuqrteredMail(MailId.CAMP_SUCC_MAIL, player);

			// 战斗失败
		} else {
			// 自己回家
			WorldMarchService.getInstance().onMarchReturn(this, atkArmyLeft, 0);
			// 刷新对方的兵力信息
			WorldMarchService.getInstance().resetMarchArmys(defMarch, defArmyLeft);
		}

		// 处理任务、统计
		sendMsgUpdateDefPlayerAfterWar(defPlayer, battleOutcome, null);

		// 敌方大本等级
		int constrFactorLvl = defPlayer.getCityLevel();
		// 进攻方战后统计
		sendMsgUpdateAtkPlayerAfterWar(isWin, atkArmyLeft, constrFactorLvl, player, battleOutcome);

		// 移除技能buff
		if (skillType.equals(BattleSkillType.BATTLE_SKILL_LIFESAVING)) {
			player.removeSkillBuff(GsConst.SKILL_10103);
		} else if (skillType.equals(BattleSkillType.BATTLE_SKILL_DUEL)) {
			player.removeSkillBuff(GsConst.SKILL_10104);
		}
		
		// 刷新战力
		refreshPowerAfterWar(atkPlayers, defPlayers);
		
		// 查找朝向目标点行军的
		Collection<IWorldMarch> worldPointMarchs = WorldMarchService.getInstance().getWorldPointMarch(worldPoint.getX(), worldPoint.getY());
		for (IWorldMarch iWorldMarch : worldPointMarchs) {
			iWorldMarch.updateMarch();
		}
	}

	/**
	 * 是否可以驻扎
	 * 
	 * @param player
	 * @param march
	 * @return
	 */
	private boolean canQuarter(Player player, WorldMarch march) {
		// 目标点id
		int pointId = march.getTerminalId();
		int[] pos = GameUtil.splitXAndY(pointId);

		// 地图最大范围
		int worldMaxX = WorldMapConstProperty.getInstance().getWorldMaxX();
		int worldMaxY = WorldMapConstProperty.getInstance().getWorldMaxY();

		// 超出地图范围
		if (pos[0] >= worldMaxX || pos[0] <= 0 || pos[1] >= worldMaxY || pos[1] <= 0) {
			WorldMarchService.logger.error("world quartered failed, out or range, x:{}, y:{}, playerId:{}, marchId:{}", pos[0], pos[1], player.getId(), march.getMarchId());
			return false;
		}

		// 请求点为阻挡点
		if (MapBlock.getInstance().isStopPoint(pointId)) {
			WorldMarchService.logger.error("world quartered failed, is stop point, x:{}, y:{}, playerId:{}, marchId:{}", pos[0], pos[1], player.getId(), march.getMarchId());
			return false;
		}
		
		Point point = new Point(pointId);
		if(!point.canQuarteredSeat()){
			WorldMarchService.logger.error("world quartered failed, x , y error, x:{}, y:{}, playerId:{}, marchId:{}", pos[0], pos[1], player.getId(), march.getMarchId());
			return false;
		}
		// 目标点
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);

		// 是否需要战斗
		boolean needFight = worldPoint == null ? false : true;
		if (needFight) {
			// 目标点类型
			int pointType = worldPoint.getPointType();

			// 不是驻扎点 || 点已经被自己占领 || 同盟玩家
			if (pointType != WorldPointType.QUARTERED_VALUE
					|| worldPoint.getPlayerId().equals(player.getId())
					|| GuildService.getInstance().isPlayerInGuild(player.getGuildId(), worldPoint.getPlayerId())) {
				return false;
			}

		} else {
			Point areaPoint = WorldPointService.getInstance().getAreaPoint(pos[0], pos[1], true);

			// 目标点不是空闲点
			if (areaPoint == null) {
				WorldMarchService.logger.error("world quartered failed, area point not free, x:{}, y:{}", pos[0], pos[1]);
				return false;
			}
		}

		return true;
	}

	/**
	 * 直接驻扎
	 */
	private void quartered(Player player, WorldPoint worldPoint, WorldMarch march) {
		this.onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE, march.getArmys(), worldPoint);
		WorldPointService.getInstance().notifyPointOccupied(march.getTerminalId(), player, this, WorldPointType.QUARTERED);
		sendQuqrteredMail(MailId.CAMP_SUCC_MAIL, player);
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_REACH_QUARTERED, Params.valueOf("march", march));
	}
	
	private void sendQuqrteredMail(MailId mailId, Player player) {
		WorldMarch march = getMarchEntity();
		int icon = GuildService.getInstance().getGuildFlagByPlayerId(player.getId());
		List<PlayerHero> heroList = player.getHeroByCfgId(march.getHeroIdList());
		int[] alarmPoint = this.alermPoint();
		FightMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(mailId).addSubTitles(alarmPoint[0], alarmPoint[1])
				.addContents(MailBuilderUtil.createSoilderAssistanceMail(march, heroList, player).setX(alarmPoint[0]).setY(alarmPoint[1]))
				.setIcon(icon)
				.build());
	}

	@Override
	public boolean needShowInGuildWar() {
		int terminalId = this.getMarchEntity().getTerminalId();
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(terminalId);
		if (worldPoint == null) {
			return false;
		}
		String targerPlayerId = worldPoint.getPlayerId();
		if (HawkOSOperator.isEmptyString(targerPlayerId)) {
			return false;
		}
		String tarGuildId = GuildService.getInstance().getPlayerGuildId(targerPlayerId);
		String ownGuildId = GuildService.getInstance().getPlayerGuildId(this.getPlayerId());
		if (HawkOSOperator.isEmptyString(ownGuildId) && HawkOSOperator.isEmptyString(tarGuildId)) {
			return false;
		}
		if (!HawkOSOperator.isEmptyString(ownGuildId)
				&& !HawkOSOperator.isEmptyString(tarGuildId)
				&& tarGuildId.equals(ownGuildId)) {
			return false;
		}
		return true;
	}

	/**
	 * 获取被动方联盟战争界面信息
	 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();

		// 队长位置
		int terminalId = this.getMarchEntity().getTerminalId();
		int[] pos = GameUtil.splitXAndY(terminalId);
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(terminalId);

		// 队长
		String leaderId = worldPoint.getPlayerId();
		Player leader = GlobalData.getInstance().makesurePlayer(leaderId);

		builder.setPointType(WorldPointType.QUARTERED);
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		builder.setLeaderArmyLimit(leader.getMaxAssistSoldier());
		builder.setGridCount(leader.getMaxMassJoinMarchNum());
		if (!HawkOSOperator.isEmptyString(leader.getGuildId())) {
			String guildTag = GuildService.getInstance().getGuildTag(leader.getGuildId());
			builder.setGuildTag(guildTag);
		}

		// 队长信息
		GuildWarSingleInfo.Builder leaderInfo = GuildWarSingleInfo.newBuilder();
		leaderInfo.setPlayerId(leader.getId());
		leaderInfo.setPlayerName(leader.getName());
		leaderInfo.setIconId(leader.getIcon());
		leaderInfo.setPfIcon(leader.getPfIcon());
		leaderInfo.setMarchStatus(WorldMarchStatus.MARCH_STATUS_WAITING);
		String marchId = worldPoint.getMarchId();
		IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
		for (ArmyInfo army : march.getMarchEntity().getArmys()) {
			leaderInfo.addArmys(army.toArmySoldierPB(leader));
		}
		builder.setLeaderMarch(leaderInfo);
		builder.setReachArmyCount(WorldUtil.calcSoldierCnt(this.getMarchEntity().getArmys()));
		return builder;
	}

	@Override
	public void remove() {
		super.remove();
		// 删除行军报告
		rePushPointReport();
	}

	@Override
	public void onMarchStart() {
		this.pushAttackReport();
		this.pullAttackReport();
	}

	@Override
	public void onMarchReturn() {
		rePushPointReport();
	}

	private void rePushPointReport() {
		// 删除行军报告
		removeAttackReport();
		// 处发其他行军重推报告
		this.pullAttackReport();
	}

	@Override
	public Set<String> attackReportRecipients() {
		Set<String> result = alarmPointEnemyMarches().stream()
				.filter(march -> march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE)
				.map(IWorldMarch::getPlayerId)
				.filter(tid -> !Objects.equals(tid, this.getMarchEntity().getPlayerId()))
				.collect(Collectors.toSet());
		return result;
	}

	@Override
	public void pullAttackReport() {
		// 不是同联盟且不是回程的行军，才会处理
		for (IWorldMarch targetMarch : alarmPointEnemyMarches()) {
			if (targetMarch instanceof IReportPushMarch) {
				((IReportPushMarch) targetMarch).pushAttackReport();
			}
		}
	}
	
	@Override
	public void pullAttackReport(String playerId) {
		// 不是同联盟且不是回程的行军，才会处理
		for (IWorldMarch targetMarch : alarmPointEnemyMarches()) {
			if (targetMarch instanceof IReportPushMarch) {
				((IReportPushMarch) targetMarch).pushAttackReport(playerId);
			}
		}

	}
	
	@Override
	public void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		// 移除向驻扎点行军的联盟战争界面信息
		if (worldPoint != null) {
			String guildId = GuildService.getInstance().getPlayerGuildId(worldPoint.getPlayerId());
			Collection<IWorldMarch> guildMarchs = WorldMarchService.getInstance().getGuildMarchs(guildId);
			if (guildMarchs != null) {
				for (IWorldMarch guildMarch : guildMarchs) {
					if (guildMarch.getMarchEntity().getTerminalId() == worldPoint.getId()) {
						WorldMarchService.getInstance().rmGuildMarch(guildMarch.getMarchId());
						continue;
					}
				}
			}
		}
		// 驻扎部队召回
		WorldPointService.getInstance().notifyQuarteredFinish(worldPoint);
		WorldMarchService.getInstance().onMarchReturn(this, callbackTime, null, getMarchEntity().getArmys(), 0, 0);
	}
	
	@Override
	public void moveCityProcess(long currentTime) {
		WorldMarchService.getInstance().accountMarchBeforeRemove(this);
		// 驻扎部队返回
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(getMarchEntity().getTerminalId());
		if (worldPoint != null && !HawkOSOperator.isEmptyString(worldPoint.getPlayerId()) && worldPoint.getPlayerId().equals(getMarchEntity().getPlayerId())) {
			WorldPointService.getInstance().notifyQuarteredFinish(worldPoint);
		}
	}
}
