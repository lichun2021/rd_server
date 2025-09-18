package com.hawk.game.world.march.submarch;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.CrossFortressCfg;
import com.hawk.game.config.SuperWeaponSoldierCfg;
import com.hawk.game.crossfortress.CrossFortressService;
import com.hawk.game.crossfortress.IFortress;
import com.hawk.game.global.GlobalData;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPeriod;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldMonsterService;
import com.hawk.game.world.service.WorldPointService;

public interface FortressMarch extends BasedMarch {

	@Override
	default boolean isFortressMarch() {
		return true;
	}

	@Override
	default void onMarchReturn() {
		WorldMarchService.getInstance().removeFortressMarch(getMarchEntity().getTerminalId(), getMarchEntity().getMarchId());
	}

	@Override
	default void onMarchReach(Player player) {
		// 统一按多人一起处理
		List<IWorldMarch> massMarchList = getMassMarchList(this);
		// 玩家联盟判断
		if (!player.hasGuild()) {
			returnMarchList(massMarchList);
			return;
		}

		Set<String> atkPlayerIds = new HashSet<>();
		for (IWorldMarch march : massMarchList) {
			atkPlayerIds.add(march.getPlayerId());
		}

		// 目标点
		int pointId = this.getMarchEntity().getTerminalId();

		IFortress fortress = CrossFortressService.getInstance().getFortress(pointId);
		if (fortress == null) {
			doPeriodChange(massMarchList);
			return;
		}

		if (CrossFortressService.getInstance().getCurrentState() != SuperWeaponPeriod.WARFARE_VALUE) {
			doPeriodChange(massMarchList);
			return;
		}

		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
		if (worldPoint == null) {
			doPeriodChange(massMarchList);
			return;
		}

		// 当前占领联盟
		String guildId = null;
		Player stayLeader = WorldMarchService.getInstance().getFortressLeader(pointId);
		if (stayLeader != null && stayLeader.hasGuild()) {
			guildId = stayLeader.getGuildId();
		}

		// 是否有npc占领
		boolean hasNpc = fortress.hasNpc();

		// 如果有npc占领
		if (hasNpc) {
			
			CrossFortressCfg crossFortressCfg = CrossFortressService.getInstance().getCrossFortressCfg(fortress.getPosX(), fortress.getPosY());
			PveBattleIncome battleIncome = BattleService.getInstance().initSuperWeaponPveBattleData(BattleConst.BattleType.ATTACK_FORTRESS_PVE, worldPoint.getId(),
					getSuperWeaponSoldierCfg(), massMarchList, crossFortressCfg.getId());
			BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
			boolean attackWin = battleOutcome.isAtkWin();
			// 据点PVE战斗邮件发放
			FightMailService.getInstance().sendPveFightMail(BattleConst.BattleType.ATTACK_FORTRESS_PVE, battleIncome, battleOutcome, null);
			// 发送战斗结果，用于前端播放动画
			WorldMarchService.getInstance().sendBattleResultInfo(this, attackWin, battleOutcome.getAftArmyMapAtk().get(player.getId()), Collections.emptyList(), false);

			if (attackWin) {
				for (IWorldMarch march : massMarchList) {
					march.getMarchEntity().setArmys(battleOutcome.getAftArmyMapAtk().get(march.getPlayerId()));
				}
				
				assitenceWarPoint(massMarchList, worldPoint, player);
				fortress.doFightNpcWin();
				fortress.doFightWin(player, null);
				// 通知场景本点数据更新
				WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
			} else {
				// 进攻方行军返回
				for (IWorldMarch march : massMarchList) {
					WorldMarchService.getInstance().onMarchReturn(march, battleOutcome.getAftArmyMapAtk().get(march.getPlayerId()), worldPoint.getId());
				}
			}
			
			String mainServerId = GlobalData.getInstance().getMainServerId(player.getServerId());
			LogUtil.logCrossFortress(mainServerId, attackWin);
			
			return;
		}

		boolean attackResult = true;
		// 本盟占领，走援助逻辑 它盟占领，走攻击逻辑
		if (!HawkOSOperator.isEmptyString(guildId) && guildId.equals(player.getGuildId())) {
			// 援助战斗点
			assitenceWarPoint(massMarchList, worldPoint, player);

		} else {
			// 超级武器里是否有行军
			boolean hasMarchBefore = WorldMarchService.getInstance().hasFortressMarch(pointId);
			// 有行军驻扎走战斗逻辑， 没有则直接驻扎
			if (hasMarchBefore) {
				boolean atkWin = attackWarPoint(massMarchList, worldPoint, player);
				if (atkWin) {
					fortress.doFightWin(player, null);					
				}
				attackResult = atkWin;				
			} else {
				fortress.doFightWin(player, null);
				for (IWorldMarch march : massMarchList) {
					march.onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE, march.getMarchEntity().getArmys(), worldPoint);
				}
			}
		}
		
		String mainServerId = GlobalData.getInstance().getMainServerId(player.getServerId());
		LogUtil.logCrossFortress(mainServerId, attackResult);
		// 通知场景本点数据更新
		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
	}

	@Override
	default void detailMarchStop(WorldPoint targetPoint) {
		// 之前没有行军占领，刷新联盟战争显示
		if (!WorldMarchService.getInstance().hasFortressMarch(targetPoint.getId())) {
			Collection<IWorldMarch> worldPointMarch = WorldMarchService.getInstance().getWorldPointMarch(targetPoint.getX(), targetPoint.getY());
			for (IWorldMarch march : worldPointMarch) {
				if (march.isReturnBackMarch() || march.isMassJoinMarch()) {
					continue;
				}
				if (GuildService.getInstance().isInTheSameGuild(march.getPlayerId(), this.getPlayerId())) {
					continue;
				}
				WorldMarchService.getInstance().addGuildMarch(march);
				march.updateMarch();
			}
		}

		WorldMarchService.getInstance().addFortressMarch(targetPoint.getId(), this, false);
	}

	/**
	 * 超级武器状态变更处理 : 发邮件, 并返回行军
	 * 
	 * @param massMarchList
	 */
	default void doPeriodChange(List<IWorldMarch> massMarchList) {
		returnMarchList(massMarchList);
	}

	/**
	 * 获取被动方联盟战争界面信息
	 */
	default GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();

		int pointId = this.getMarchEntity().getTerminalId();
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		builder.setPointType(WorldPointType.CROSS_FORTRESS);
		builder.setX(point.getX());
		builder.setY(point.getY());

		// 队长id
		Player leader = WorldMarchService.getInstance().getFortressLeader(pointId);
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
		String leaderMarchId = WorldMarchService.getInstance().getFortressLeaderMarchId(pointId);
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
		for (ArmyInfo army : leaderMarch.getMarchEntity().getArmys()) {
			leaderInfo.addArmys(army.toArmySoldierPB(leader));
		}
		leaderInfo.setMarchId(leaderMarchId);
		builder.setLeaderMarch(leaderInfo);

		builder.setLeaderArmyLimit(leaderMarch.getMaxMassJoinSoldierNum(leader));
		// 已经到达的士兵数量
		int reachArmyCount = WorldUtil.calcSoldierCnt(leaderMarch.getMarchEntity().getArmys());

		List<IWorldMarch> marchs = WorldMarchService.getInstance().getFortressStayMarchs(pointId);
		for (IWorldMarch stayMarch : marchs) {
			if (stayMarch.getMarchId().equals(leaderMarchId)) {
				continue;
			}
			builder.addJoinMarchs(getGuildWarSingleInfo(stayMarch.getMarchEntity()));
			reachArmyCount += WorldUtil.calcSoldierCnt(stayMarch.getMarchEntity().getArmys());
		}
		builder.setReachArmyCount(reachArmyCount);
		return builder;
	}

	/**
	 * 获取npc驻守兵力
	 * 
	 * @return
	 */
	default SuperWeaponSoldierCfg getSuperWeaponSoldierCfg() {
		int worldMaxLevel = WorldMonsterService.getInstance().getMaxCommonMonsterLvl();
		SuperWeaponSoldierCfg config = HawkConfigManager.getInstance().getConfigByKey(SuperWeaponSoldierCfg.class, worldMaxLevel);
		if (config == null) {
			int configSize = HawkConfigManager.getInstance().getConfigSize(SuperWeaponSoldierCfg.class);
			config = HawkConfigManager.getInstance().getConfigByIndex(SuperWeaponSoldierCfg.class, configSize - 1);
		}
		return config;
	}

	@Override
	public default Set<IWorldMarch> getQuarterMarch() {
		Set<IWorldMarch> retMarchs = new HashSet<>();
		
		if (this.isReachAndStopMarch()) {
			BlockingDeque<String> marchIds = WorldMarchService.getInstance().getFortressMarchs(this.getAlarmPointId());
			for (String marchId : marchIds) {
				retMarchs.add(WorldMarchService.getInstance().getMarch(marchId));
			}
		}
		return retMarchs;
	}
	
}
