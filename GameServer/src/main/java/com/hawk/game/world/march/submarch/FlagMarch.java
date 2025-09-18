package com.hawk.game.world.march.submarch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.WarFlag.FlageState;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.service.flag.FlagCollection;
import com.hawk.game.service.warFlag.IFlag;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 战旗行军
 *
 */
public interface FlagMarch extends BasedMarch {
	
	@Override
	default boolean isWarFlagMarch() {
		return true;
	}
	
	@Override
	default void onMarchReach(Player player) {
		
		// 统一按多人一起处理
		List<IWorldMarch> massMarchList = getMassMarchList(this);
		
		boolean activityOpen = WarFlagService.getInstance().isActivityOpen();
		if (!activityOpen) {
			returnMarchList(massMarchList);
			return;
		}

		if (!player.hasGuild()) {
			returnMarchList(massMarchList);
			return;
		}
		
		WorldPoint flagPoint = WorldPointService.getInstance().getWorldPoint(this.getTerminalId());
		if (flagPoint == null) {
			returnMarchList(massMarchList);
			return;
		}

		if (flagPoint.getPointType() != WorldPointType.WAR_FLAG_POINT_VALUE) {
			returnMarchList(massMarchList);
			return;
		}

		String flagId = flagPoint.getGuildBuildId();
		if (HawkOSOperator.isEmptyString(flagId)) {
			returnMarchList(massMarchList);
			return;
		}

		IFlag flag = FlagCollection.getInstance().getFlag(flagId);
		if (flag == null) {
			returnMarchList(massMarchList);
			return;
		}

		// 战旗行军状态判断
		if (flag.getState() != FlageState.FLAG_DEFEND_VALUE
				&& flag.getState() != FlageState.FLAG_FIX_VALUE
				&& flag.getState() != FlageState.FLAG_DAMAGED_VALUE
				&& flag.getState() != FlageState.FLAG_BEINVADED_VALUE) {
			returnMarchList(massMarchList);
			return;
		}
		
		for (IWorldMarch march : massMarchList) {
			march.getMarchEntity().setTargetId(flagId);
		}
		
		Set<String> atkPlayerIds = new HashSet<>();
		for (IWorldMarch march : massMarchList) {
			atkPlayerIds.add(march.getPlayerId());
		}
		
		flagMarchReach(flag, massMarchList);
		
		// 战旗点
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(getMarchEntity().getTerminalId());
		// 通知场景本点数据更新
		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
	}
	
	default void flagMarchReach(IFlag flag, List<IWorldMarch> massMarch) {
		
		switch (flag.getState()) {

		// 防守中
		case FlageState.FLAG_DEFEND_VALUE:
		case FlageState.FLAG_FIX_VALUE:
		case FlageState.FLAG_DAMAGED_VALUE:
			compStateReach(flag, massMarch);
			break;
			
		// 摧毁中
		case FlageState.FLAG_BEINVADED_VALUE:
			beinvadedStateReach(flag, massMarch);
			break;
			
		default:
			returnMarchList(massMarch);
			break;
		}
		
	}
	
	/**
	 * 已完成状态到达
	 * @param flag
	 */
	default void compStateReach(IFlag flag, List<IWorldMarch> massMarch) {
		if (!WarFlagService.getInstance().canFlagFight(flag, this.getPlayer().getGuildId())) {
			returnMarchList(massMarch);
			return;
		}
		
		List<IWorldMarch> marchs = WarFlagService.getInstance().getFlagPointMarch(flag);
		boolean hasMarchInPoint = !marchs.isEmpty();
		boolean isAtkMarch = !this.getPlayer().getGuildId().equals(flag.getCurrentId());
		
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(flag.getPointId());
		if (point == null) {
			returnMarchList(massMarch);
			return;	
		}
		
		String winGuildId = flag.getOwnerId();
		
		// 防守行军到达，发现点上有自己盟友的行军直接驻扎
		if (!isAtkMarch) {
			assitenceWarPoint(massMarch, point, getPlayer());
			for (IWorldMarch march : WarFlagService.getInstance().getFlagPointMarch(flag)) {
				if (flag.getState() == FlageState.FLAG_DEFEND_VALUE) {
					march.onMarchStop(WorldMarchStatus.MARCH_STATUS_MANOR_GARRASION_VALUE, march.getMarchEntity().getArmys(), point);
				} else {
					march.onMarchStop(WorldMarchStatus.MARCH_STATUS_MANOR_REPAIR_VALUE, march.getMarchEntity().getArmys(), point);
				}
			}
			flag.marchReach(winGuildId);
			return;		
		}
		
		// 攻击行军到达，发现点上没有行军，直接进入摧毁
		else if (!hasMarchInPoint && isAtkMarch) {
			assitenceWarPoint(massMarch, point, getPlayer());
			for (IWorldMarch march : WarFlagService.getInstance().getFlagPointMarch(flag)) {
				march.onMarchStop(WorldMarchStatus.MARCH_STATUS_OCCUPY_VALUE, march.getMarchEntity().getArmys(), point);
			}
			winGuildId = getPlayer().getGuildId();
		}

		// 攻击行军到达，发现点上有行军，发生战斗
		else if (hasMarchInPoint && isAtkMarch) {
			boolean isAtkWin = attackWarPoint(massMarch, point, getPlayer());
			if (!isAtkWin) {
				winGuildId = flag.getCurrentId();
			} else {
				winGuildId = getPlayer().getGuildId();
				for (IWorldMarch march : WarFlagService.getInstance().getFlagPointMarch(flag)) {
					march.onMarchStop(WorldMarchStatus.MARCH_STATUS_OCCUPY_VALUE, march.getMarchEntity().getArmys(), point);
				}
			}
		}
		
		flag.marchReach(winGuildId);
	}
	
	
	/**
	 * 摧毁状态行军到达
	 */
	default void beinvadedStateReach(IFlag flag, List<IWorldMarch> massMarch) {

		List<IWorldMarch> marchs = WarFlagService.getInstance().getFlagPointMarch(flag);
		boolean hasMarchInPoint = !marchs.isEmpty();
		// 是否是收复行军
		boolean isRecoverMarch = this.getPlayer().getGuildId().equals(flag.getCurrentId());

		WorldPoint point = WorldPointService.getInstance().getWorldPoint(flag.getPointId());
		if (point == null) {
			returnMarchList(massMarch);
			return;	
		}
		
		if (!hasMarchInPoint) {
			compStateReach(flag, massMarch);
			return;
		}
		
		// 摧毁行军到达
		if (!isRecoverMarch) {
			
			// 自己盟有人在摧毁中，直接返回
			if (this.getPlayer().getGuildId().equals(marchs.get(0).getPlayer().getGuildId())) {
				assitenceWarPoint(massMarch, point, getPlayer());
				for (IWorldMarch march : WarFlagService.getInstance().getFlagPointMarch(flag)) {
					march.onMarchStop(WorldMarchStatus.MARCH_STATUS_OCCUPY_VALUE, march.getMarchEntity().getArmys(), point);
				}
				return;
			}
			
			// 其他盟在摧毁中，与其他盟战斗
			boolean isAtkWin = attackWarPoint(massMarch, point, getPlayer());
			if (isAtkWin) {
				for (IWorldMarch march : WarFlagService.getInstance().getFlagPointMarch(flag)) {
					march.onMarchStop(WorldMarchStatus.MARCH_STATUS_OCCUPY_VALUE, march.getMarchEntity().getArmys(), point);
				}
				flag.marchReach(this.getPlayer().getGuildId());
			}
			return;
		}
		
		// 收复行军到达，发现点上有行军，发生战斗
		if (isRecoverMarch) {
			
			Player atkPlayer = this.getPlayer();
			Player defPlayer = marchs.get(0).getPlayer();
			
			// 自己盟有人在摧毁中，直接返回
			if (atkPlayer.getGuildId().equals(defPlayer.getGuildId())) {
				returnMarchList(massMarch);
				return;
			}
			
			// 否则与敌人发生战斗
			boolean isAtkWin = attackWarPoint(massMarch, point, getPlayer());
			if (isAtkWin) {
				for (IWorldMarch march : WarFlagService.getInstance().getFlagPointMarch(flag)) {
					march.onMarchStop(WorldMarchStatus.MARCH_STATUS_MANOR_REPAIR_VALUE, march.getMarchEntity().getArmys(), point);
				}
				flag.marchReach(this.getPlayer().getGuildId());
			}
		}
	}
	
	/**
	 * 获取被动方联盟战争界面信息
	 */
	default GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		
		int pointId = this.getMarchEntity().getTerminalId();
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		
		IFlag flag = FlagCollection.getInstance().getFlag(point.getGuildBuildId());
		
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		
		builder.setPointType(WorldPointType.WAR_FLAG_POINT);
		builder.setX(point.getX());
		builder.setY(point.getY());
		builder.setIsCenter(flag.isCenter());
		
		// 队长id
		Player leader = WorldMarchService.getInstance().getFlagLeader(point.getGuildBuildId());
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
		String leaderMarchId = WorldMarchService.getInstance().getFlagLeaderMarchId(point.getGuildBuildId());
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
		for (ArmyInfo army : leaderMarch.getMarchEntity().getArmys()) {
			leaderInfo.addArmys(army.toArmySoldierPB(leader));
		}
		leaderInfo.setMarchId(leaderMarchId);
		builder.setLeaderMarch(leaderInfo);
		
		builder.setLeaderArmyLimit(leaderMarch.getMaxMassJoinSoldierNum(leader));
		// 已经到达的士兵数量
		int reachArmyCount = WorldUtil.calcSoldierCnt(leaderMarch.getMarchEntity().getArmys());
		
		List<IWorldMarch> marchs = WarFlagService.getInstance().getFlagPointMarch(flag);
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
	
	@Override
	default boolean needShowInGuildWar() {
		int pointId = this.getMarchEntity().getTerminalId();
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		if (point == null) {
			return false;
		}
		
		IFlag flag = FlagCollection.getInstance().getFlag(point.getGuildBuildId());
		if (flag == null) {
			return false;
		}
		
		if (this.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			return false;
		}
		
		return true;
	}
	
	@Override
	default void detailMarchStop(WorldPoint targetPoint) {
		WorldMarchService.getInstance().addFlagMarchs(targetPoint.getGuildBuildId(), this, false);
	}
	
	/**
	 * 召回
	 */
	@Override
	default void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		flagMarchReturn();
		
		int[] pos = GameUtil.splitXAndY(worldPoint.getId());
		WorldPointService.getInstance().notifyPointUpdate(pos[0], pos[1]);
	}
	
	/**
	 * 行军返回
	 */
	default void flagMarchReturn() {
		if (this.isReturnBackMarch()) {
			return;
		}
		
		// 开始返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
	}
	
	
	/**
	 * 迁城
	 */
	@Override
	default void moveCityProcess(long currentTime) {
		WorldMarchService.getInstance().accountMarchBeforeRemove(this);
		flagMarchReturn();
	}
	
	/**
	 * 行军返回开始
	 */
	@Override
	default void onMarchReturn() {
		IFlag flag = FlagCollection.getInstance().getFlag(this.getMarchEntity().getTargetId());
		if (flag != null) {
			flag.marchReturn();
		}
	}
}
