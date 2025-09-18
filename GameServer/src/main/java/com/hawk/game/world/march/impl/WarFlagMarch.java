package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.stream.Collectors;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.PvpBannerBattleEvent;
import com.hawk.game.battle.BattleCalcParames;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.IBattleIncome;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.item.AwardItems;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.WarFlag.FlageState;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.service.flag.FlagCollection;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.warFlag.IFlag;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 战旗行军
 * @author golden
 *
 */
public class WarFlagMarch extends PlayerMarch implements BasedMarch ,IReportPushMarch, IPassiveAlarmTriggerMarch{

	static Logger logger = LoggerFactory.getLogger("Server");

	public WarFlagMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public boolean isWarFlagMarch() {
		return true;
	}
	
	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.WAR_FLAG_MARCH;
	}

	@Override
	public void detailMarchStop(WorldPoint targetPoint) {
		WorldMarchService.getInstance().addFlagMarchs(targetPoint.getGuildBuildId(), this, false);
	}
	
	@Override
	public void onMarchStart() {
		this.pushAttackReport();
		// 不是同联盟且不是回程的行军，才会处理
		this.pullAttackReport();
	}
	
	private void rePushPointReport() {
		// 删除行军报告
		removeAttackReport();
		this.pullAttackReport();
	}
	
	@Override
	public void remove() {
		super.remove();
		// 删除行军报告
		rePushPointReport();
	}
	
	@Override
	public void pullAttackReport() {
		for (IWorldMarch targetMarch : alarmPointEnemyMarches()) {
			if (targetMarch instanceof IReportPushMarch) {
				((IReportPushMarch) targetMarch).pushAttackReport();
			}
		}
	}
	
	@Override
	public void pullAttackReport(String playerId) {
		for (IWorldMarch targetMarch : alarmPointEnemyMarches()) {
			if (targetMarch instanceof IReportPushMarch) {
				((IReportPushMarch) targetMarch).pushAttackReport(playerId);
			}
		}
	}
	
	@Override
	public Set<String> attackReportRecipients() {
		Set<String> result = alarmPointEnemyMarches().stream()
				.filter(march -> march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_GARRASION_VALUE ||
						march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_OCCUPY_VALUE ||
						march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_REPAIR_VALUE ||
						march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE)
				.map(IWorldMarch::getPlayerId)
				.filter(tid -> !Objects.equals(tid, this.getMarchEntity().getPlayerId()))
				.collect(Collectors.toSet());
		return result;
	}
	
	@Override
	public void onMarchReach(Player player) {
		
		boolean activityOpen = WarFlagService.getInstance().isActivityOpen();
		if (!activityOpen) {
			flagMarchReturn();
			return;
		}

		if (!player.hasGuild()) {
			flagMarchReturn();
			return;
		}
		
		WorldPoint flagPoint = WorldPointService.getInstance().getWorldPoint(this.getTerminalId());
		if (flagPoint == null) {
			flagMarchReturn();
			return;
		}

		if (flagPoint.getPointType() != WorldPointType.WAR_FLAG_POINT_VALUE) {
			flagMarchReturn();
			return;
		}

		String flagId = flagPoint.getGuildBuildId();
		if (HawkOSOperator.isEmptyString(flagId)) {
			flagMarchReturn();
			return;
		}

		IFlag flag = FlagCollection.getInstance().getFlag(flagId);
		if (flag == null) {
			flagMarchReturn();
			return;
		}

		this.getMarchEntity().setTargetId(flagId);
		
		flagMarchReach(flag);
		rePushPointReport();
	}

	public void flagMarchReach(IFlag flag) {
		
		switch (flag.getState()) {

		// 已放置建造中
		case FlageState.FLAG_PLACED_VALUE:
		case FlageState.FLAG_BUILDING_VALUE:
			buildingStateReach(flag);
			break;
			
		// 防守中
		case FlageState.FLAG_DEFEND_VALUE:
		case FlageState.FLAG_FIX_VALUE:
		case FlageState.FLAG_DAMAGED_VALUE:
			compStateReach(flag);
			break;
			
		// 摧毁中
		case FlageState.FLAG_BEINVADED_VALUE:
			beinvadedStateReach(flag);
			break;
			
		default:
			flagMarchReturn();
			break;
		}
		
	}

	/**
	 * 建造状态到达
	 */
	public void buildingStateReach(IFlag flag) {
		if (!this.getPlayer().getGuildId().equals(flag.getCurrentId())) {
			flagMarchReturn();
			return;
		}
		
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(flag.getPointId());
		if (point == null) {
			flagMarchReturn();
			return;	
		}
		
		if (hasMarchInFlag(flag)) {
			List<IWorldMarch> marchs = new ArrayList<>();
			marchs.add(this);
			assitenceWarPoint(marchs, point, this.getPlayer());
		} else {
			onMarchStop(WorldMarchStatus.MARCH_STATUS_MANOR_BUILD_VALUE, this.getMarchEntity().getArmys(), point);
		}
		
		flag.marchReach(this.getPlayer().getGuildId());
		
		logger.info("warFlag march reach, flagLife:{}, speed:{}, overTime:{}, remainTime:{}", flag.getLife(), flag.getSpeed(), this.getEndTime(), this.getEndTime() - HawkTime.getMillisecond());
	}

	/**
	 * 是否有自己的行军在建造中
	 */
	private boolean hasMarchInFlag(IFlag flag) {
		BlockingDeque<String> marchIds = WorldMarchService.getInstance().getFlagMarchs(flag.getFlagId());
		return !marchIds.isEmpty();
	}
	
	/**
	 * 已完成状态到达
	 * @param flag
	 */
	public void compStateReach(IFlag flag) {
		if (!WarFlagService.getInstance().canFlagFight(flag, this.getPlayer().getGuildId())) {
			flagMarchReturn();
			return;
		}
		
		List<IWorldMarch> marchs = WarFlagService.getInstance().getFlagPointMarch(flag);
		boolean hasMarchInPoint = !marchs.isEmpty();
		boolean isAtkMarch = !this.getPlayer().getGuildId().equals(flag.getCurrentId());
		
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(flag.getPointId());
		if (point == null) {
			flagMarchReturn();
			return;	
		}
		
		// 防守行军到达，发现点上有自己盟友的行军直接返回
		if (hasMarchInPoint && !isAtkMarch) {
			List<IWorldMarch> thisMarchs = new ArrayList<>();
			thisMarchs.add(this);
			assitenceWarPoint(thisMarchs, point, this.getPlayer());
			flag.marchReach(this.getPlayer().getGuildId());
			return;		
		}
		
		String winGuildId = this.getPlayer().getGuildId();
		
		// 防守行军到达，发现点上没有自己盟友的行军直接驻扎
		if (!hasMarchInPoint && !isAtkMarch) {
			if (flag.getState() == FlageState.FLAG_DEFEND_VALUE) {
				onMarchStop(WorldMarchStatus.MARCH_STATUS_MANOR_GARRASION_VALUE, this.getMarchEntity().getArmys(), point);
			} else {
				onMarchStop(WorldMarchStatus.MARCH_STATUS_MANOR_REPAIR_VALUE, this.getMarchEntity().getArmys(), point);
			}
		}
		
		// 攻击行军到达，发现点上没有行军，直接进入摧毁
		else if (!hasMarchInPoint && isAtkMarch) {
			onMarchStop(WorldMarchStatus.MARCH_STATUS_OCCUPY_VALUE, this.getMarchEntity().getArmys(), point);
		}

		// 攻击行军到达，发现点上有行军，发生战斗
		else if (hasMarchInPoint && isAtkMarch) {
			boolean isAtkWin = pvpInWarFlag(flag);
			if (!isAtkWin) {
				winGuildId = flag.getCurrentId();
			}
		}
		
		flag.marchReach(winGuildId);
	}
	
	/**
	 * 摧毁状态行军到达
	 */
	public void beinvadedStateReach(IFlag flag) {

		List<IWorldMarch> marchs = WarFlagService.getInstance().getFlagPointMarch(flag);
		boolean hasMarchInPoint = !marchs.isEmpty();
		// 是否是收复行军
		boolean isRecoverMarch = this.getPlayer().getGuildId().equals(flag.getCurrentId());

		WorldPoint point = WorldPointService.getInstance().getWorldPoint(flag.getPointId());
		if (point == null) {
			flagMarchReturn();
			return;	
		}
		
		// 摧毁行军到达
		if (hasMarchInPoint && !isRecoverMarch) {
			
			// 自己盟有人在摧毁中，直接返回
			if (this.getPlayer().getGuildId().equals(marchs.get(0).getPlayer().getGuildId())) {
				flagMarchReturn();
				return;
			}
			
			if (this.getPlayer().getId().equals(marchs.get(0).getPlayerId())) {
				flagMarchReturn();
				return;
			}
			
			// 其他盟在摧毁中，与其他盟战斗
			boolean isAtkWin = pvpInWarFlag(flag);
			if (isAtkWin) {
				flag.marchReach(this.getPlayer().getGuildId());
			}
			return;
		}
		
		// 收复行军到达，发现点上有行军，发生战斗
		if (hasMarchInPoint && isRecoverMarch) {
			
			Player atkPlayer = this.getPlayer();
			Player defPlayer = marchs.get(0).getPlayer();
			
			// 自己盟有人在摧毁中，直接返回
			if (atkPlayer.getGuildId().equals(defPlayer.getGuildId())) {
				flagMarchReturn();
				return;
			}
			
			if (this.getPlayer().getId().equals(marchs.get(0).getPlayerId())) {
				flagMarchReturn();
				return;
			}
			
			// 否则与敌人发生战斗
			boolean isAtkWin = pvpInWarFlag(flag);
			
			if (isAtkWin) {
				this.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MANOR_REPAIR_VALUE);
				this.updateMarch();
				flag.marchReach(this.getPlayer().getGuildId());
			}
		}
	}
	
	/**
	 * 旗子上进行pvp战斗
	 */
	public boolean pvpInWarFlag(IFlag flag) {
		
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(flag.getPointId());
		
		List<IWorldMarch> marchs = WarFlagService.getInstance().getFlagPointMarch(flag);
		
		Player atkPlayer = this.getPlayer();
		Player defPlayer = marchs.get(0).getPlayer();
		
		// 进攻方玩家
		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(atkPlayer);

		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		for (IWorldMarch defMarch : marchs) {
			defPlayers.add(defMarch.getPlayer());
		}

		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);

		// 防守方行军
		List<IWorldMarch> defMarchs = new ArrayList<>();
		for (IWorldMarch defMarch : marchs) {
			defMarchs.add(defMarch);
		}

		// 战斗数据输入
		boolean isAtkOwner = atkPlayer.hasGuild() && atkPlayer.getGuildId().equals(flag.getCurrentId());
		boolean isDefOwner = defPlayer.hasGuild() && defPlayer.getGuildId().equals(flag.getCurrentId());
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.ATTACK_WAR_FLAG, point.getId(), atkPlayers, defPlayers, atkMarchs, defMarchs, null, isAtkOwner, isDefOwner);
		
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);

		// 战斗胜利
		boolean isAtkWin = battleOutcome.isAtkWin();
		
		// 发送战斗邮件
		FightMailService.getInstance().sendFightMail(point.getPointType(), battleIncome, battleOutcome, null);
		
		BattleService.getInstance().dealWithPvpBattleEvent(battleIncome, battleOutcome, false, this.getMarchType());
		
		// 双方战后剩余部队
		Map<String, List<ArmyInfo>> atkArmyLeftMap = battleOutcome.getAftArmyMapAtk();
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();
		List<ArmyInfo> atkArmyLeft = atkArmyLeftMap.get(atkPlayer.getId());

		// 发送战斗结果，用于前端播放动画
		WorldMarchService.getInstance().sendBattleResultInfo(this, isAtkWin, atkArmyLeft, defArmyLeftMap.get(defPlayer.getId()), isAtkWin);
		
		// 战旗争夺战结果事件
		postWarFlagResultEvent(battleIncome, battleOutcome);
		
		if (isAtkWin) {
			for (IWorldMarch defMarch : marchs) {
				WorldMarchService.getInstance().onMarchReturn(defMarch, AwardItems.valueOf(), defArmyLeftMap.get(defMarch.getPlayer().getId()), 0);
			}
			onMarchStop(WorldMarchStatus.MARCH_STATUS_OCCUPY_VALUE, atkArmyLeft, point);
		} else {
			for (IWorldMarch defMarch : marchs) {
				WorldMarchService.getInstance().resetMarchArmys(defMarch, defArmyLeftMap.get(defMarch.getPlayer().getId()));
			}
			WorldMarchService.getInstance().onMarchReturn(this, atkArmyLeft, point.getId());
		}

		// 处理任务、统计
		for (IWorldMarch defMarch : marchs) {
			sendMsgUpdateDefPlayerAfterWar(defMarch.getPlayer(), battleOutcome, null);
		}
		
		sendMsgUpdateAtkPlayerAfterWar(isAtkWin, atkArmyLeft,  defPlayer.getCityLevel(), atkPlayer, battleOutcome);
		
		refreshPowerAfterWar(atkPlayers, defPlayers);
		
		return isAtkWin;
	}
	
	/**
	 * 计算处理攻防部队击杀/击伤数据
	 */
	private void postWarFlagResultEvent(IBattleIncome battleIncome, BattleOutcome battleOutcome) {
		try {
			// 计算进攻方玩家部队击杀/击伤信息
			calcArmyInfo(battleIncome.getAtkCalcParames(), battleOutcome.getBattleArmyMapAtk(), battleOutcome.getBattleArmyMapDef(), true);
			// 计算防御方玩家部队击杀/击伤信息
			calcArmyInfo(battleIncome.getDefCalcParames(), battleOutcome.getBattleArmyMapDef(), battleOutcome.getBattleArmyMapAtk(), false);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 计算处理部队击杀击伤数据
	 */
	private void calcArmyInfo(BattleCalcParames calcParames, Map<String, List<ArmyInfo>> selfBattleArmyMap, Map<String, List<ArmyInfo>> oppBattleArmyMap, boolean isAtk) {
		Map<String, Map<Integer, Integer>> armyKillMap = new HashMap<>();
		Map<String, Map<Integer, Integer>> armyHurtMap = new HashMap<>();
		
		// 存在歼灭等情况,都按照混合类型计算
		BattleService.getInstance().calcKillAndHurtInfo(armyKillMap, armyHurtMap, selfBattleArmyMap, oppBattleArmyMap);
		
		// 给参战玩家推送PVP战斗事件
		for (String playerId : selfBattleArmyMap.keySet()) {
			Map<Integer, Integer> killMap = armyKillMap.get(playerId);
			if (killMap == null) {
				killMap = new HashMap<>();
			}
			Map<Integer, Integer> hurtMap = armyHurtMap.get(playerId);
			if (hurtMap == null) {
				hurtMap = new HashMap<>();
			}
			
			// 联盟旗帜战斗事件
			ActivityManager.getInstance().postEvent(new PvpBannerBattleEvent(playerId, killMap, hurtMap, isAtk));
		}
	}
	
	/**
	 * 行军返回
	 */
	public void flagMarchReturn() {
		if (this.isReturnBackMarch()) {
			return;
		}
		
		// 开始返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
	}
	
	/**
	 * 行军返回开始
	 */
	@Override
	public void onMarchReturn() {
		
		rePushPointReport();
		
		IFlag flag = FlagCollection.getInstance().getFlag(this.getMarchEntity().getTargetId());
		flag.marchReturn();
	}
	
	/**
	 * 迁城
	 */
	@Override
	public void moveCityProcess(long currentTime) {
		WorldMarchService.getInstance().accountMarchBeforeRemove(this);
		flagMarchReturn();
	}
	
	/**
	 * 召回
	 */
	@Override
	public void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		flagMarchReturn();
		
		int[] pos = GameUtil.splitXAndY(worldPoint.getId());
		WorldPointService.getInstance().notifyPointUpdate(pos[0], pos[1]);
	}
	
	/**
	 * 是否需要显示在联盟战争界面
	 */
	@Override
	public boolean needShowInGuildWar() {
		String flagId = this.getMarchEntity().getTargetId();
		
		IFlag flag = FlagCollection.getInstance().getFlag(flagId);
		if (flag == null) {
			return false;
		}
		
		if (!this.getPlayer().hasGuild()) {
			return false;
		}
		
		if (this.getPlayer().getGuildId().equals(flag.getCurrentId())) {
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
		
		String flagId = this.getMarchEntity().getTargetId();
		IFlag flag = FlagCollection.getInstance().getFlag(flagId);
		if (flag == null) {
			return builder;
		}
		
		WorldPoint towerPoint = WorldPointService.getInstance().getWorldPoint(flag.getPointId());
		builder.setPointType(WorldPointType.CAPITAL_TOWER);
		builder.setX(towerPoint.getX());
		builder.setY(towerPoint.getY());
		builder.setIsCenter(flag.isCenter());
		
		// 队长id
		Player leader = WorldMarchService.getInstance().getFlagLeader(flagId);
		if (leader == null) {
			return builder;
		}
		
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
		leaderInfo.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		
		String leaderMarchId = WorldMarchService.getInstance().getFlagLeaderMarchId(flagId);
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
		for (ArmyInfo army : leaderMarch.getMarchEntity().getArmys()) {
			leaderInfo.addArmys(army.toArmySoldierPB(leader));
		}
		leaderInfo.setMarchId(leaderMarchId);
		builder.setLeaderMarch(leaderInfo);
		
		// 已经到达的士兵数量
		int reachArmyCount = WorldUtil.calcSoldierCnt(leaderMarch.getMarchEntity().getArmys());
		
		BlockingDeque<String> marchIds = WorldMarchService.getInstance().getFlagMarchs(flagId); 
		for (String marchId : marchIds) {
			if (marchId.equals(leaderMarchId)) {
				continue;
			}
			IWorldMarch stayMarch = WorldMarchService.getInstance().getMarch(marchId);
			builder.addJoinMarchs(getGuildWarSingleInfo(stayMarch.getMarchEntity()));
			reachArmyCount += WorldUtil.calcSoldierCnt(stayMarch.getMarchEntity().getArmys());
		}
		builder.setLeaderArmyLimit(leaderMarch.getMaxMassJoinSoldierNum(leader));
		builder.setReachArmyCount(reachArmyCount);
		return builder;
	}
}
