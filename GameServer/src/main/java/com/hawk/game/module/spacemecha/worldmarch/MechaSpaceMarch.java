package com.hawk.game.module.spacemecha.worldmarch;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.config.SpaceMechaCabinCfg;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 联盟机甲舱体守护行军
 * 
 * @author lating
 *
 */
public interface MechaSpaceMarch extends BasedMarch {
	
	@Override
	default boolean isGuildSpaceMarch() {
		return true;
	}
	
	/**
	 * 判断状态是否匹配
	 * @return
	 */
	public boolean isSpaceStateMatch();
	/**
	 * 行军限制判断
	 * @return
	 */
	public boolean marchCountLimitCheck();
	/**
	 * 判断是否是自己联盟的舱体
	 * 
	 * @return
	 */
	public boolean isSelfGuildSpace();
	/**
	 * 点类型判断
	 * @return
	 */
	public boolean pointCheck();
	
	@Override
	default void onMarchReach(Player player) {
		// 统一按多人一起处理
		List<IWorldMarch> massMarchList = getMassMarchList(this);
		// 玩家联盟判断
		if (!player.hasGuild()) {
			returnMarchList(massMarchList);
			return;
		}
		
		// 未放置联盟机甲舱体
		MechaSpaceInfo spaceInfo = SpaceMechaService.getInstance().getGuildSpace(player.getGuildId());
		if (spaceInfo == null) {
			returnMarchList(massMarchList);
			return;
		}
		
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(this.getTerminalId());
		if (worldPoint == null) {
			returnMarchList(massMarchList);
        	return;
		}
		
        if (!pointCheck()) {
        	returnMarchList(massMarchList);
        	return;
        }
        
        // 不是自己联盟放置的那个舱体
        if (!isSelfGuildSpace()) {
        	returnMarchList(massMarchList);
			return;
        }
        
        // 状态不对
        if (!isSpaceStateMatch()) {
        	returnMarchList(massMarchList);
			return;
        }
        
        SpaceWorldPoint spacePoint = (SpaceWorldPoint) worldPoint;
        HawkLog.logPrintln("spaceMecha player defMarch reach, guildId: {}, playerId: {}, marchId: {}, spaceIndex: {}, posX: {}, poxY: {}, remain blood: {}", 
        		player.getGuildId(), player.getId(), this.getMarchId(), spacePoint.getSpaceIndex(), worldPoint.getX(), worldPoint.getY(), spacePoint.getSpaceBlood());
		
		boolean notEmpty = spaceInfo.hasMarchInSpace(spacePoint.getSpaceIndex());
		if (!notEmpty) {
			for (IWorldMarch march : massMarchList) {
				march.onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE, march.getMarchEntity().getArmys(), worldPoint);
			}
		} else {
			if (!marchCountLimitCheck()) {
				returnMarchList(massMarchList);
				return;
			}
			
			// 援助战斗点
			assitenceWarPoint(massMarchList, worldPoint, player);
		}
		
		// 通知场景本点数据更新
		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
	}
	
	@Override
	default void detailMarchStop(WorldPoint targetPoint) {
		String playerId = this.getMarchEntity().getPlayerId();
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			guildId = GuildService.getInstance().getPlayerGuildId(this.getMarchEntity().getLeaderPlayerId());
		}
		
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(guildId);
		SpaceWorldPoint spacePoint = (SpaceWorldPoint) targetPoint;
		if (!spaceObj.hasMarchInSpace(spacePoint.getSpaceIndex())) {
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
		spaceObj.addSpaceMarch(spacePoint.getSpaceIndex(), this);
	}
	

	/**
	 * 获取防守方联盟战争界面信息
	 */
	default GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		int pointId = this.getMarchEntity().getTerminalId();
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		SpaceWorldPoint spacePoint = (SpaceWorldPoint) point;
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		builder.setPointType(WorldPointType.valueOf(point.getPointType()));  // -- 1
		builder.setX(spacePoint.getX());  // -- 2
		builder.setY(spacePoint.getY());  // -- 3

		String playerId = this.getMarchEntity().getPlayerId();
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(guildId);
		SpaceMechaCabinCfg cfg = SpaceMechaCabinCfg.getCfgByLevel(spacePoint.getSpaceLevel());
		builder.setMonsterId(cfg.getId());
		// 队长id
		Player leader = spaceObj.getSpaceLeader(spacePoint.getSpaceIndex());
		if (leader == null) {
			return builder;
		}
		builder.setGridCount(leader.getMaxMassJoinMarchNum());  // -- 5
		String guildTag = GuildService.getInstance().getGuildTag(leader.getGuildId());  // -- 6
		builder.setGuildTag(guildTag);

		// 队长信息
		GuildWarSingleInfo.Builder leaderInfo = GuildWarSingleInfo.newBuilder();
		leaderInfo.setPlayerId(leader.getId());
		leaderInfo.setPlayerName(leader.getName());
		leaderInfo.setIconId(leader.getIcon());
		leaderInfo.setPfIcon(leader.getPfIcon());
		leaderInfo.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		String leaderMarchId = spaceObj.getSpaceLeaderMarchId(spacePoint.getSpaceIndex());
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
		for (ArmyInfo army : leaderMarch.getMarchEntity().getArmys()) {
			leaderInfo.addArmys(army.toArmySoldierPB(leader));
		}
		leaderInfo.setMarchId(leaderMarchId);
		builder.setLeaderMarch(leaderInfo);
		
		builder.setLeaderArmyLimit(leaderMarch.getMaxMassJoinSoldierNum(leader));
		// 已经到达的士兵数量
		int reachArmyCount = WorldUtil.calcSoldierCnt(leaderMarch.getMarchEntity().getArmys());
		
		List<IWorldMarch> marchs = spaceObj.getSpaceMarchs(spacePoint.getSpaceIndex());
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
	
	default void moveCityProcess(long currentTime) {
		WorldMarchService.getInstance().accountMarchBeforeRemove(this);
		int terminalId = this.getMarchEntity().getTerminalId();
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(terminalId);
		// 如果行军已驻扎下来了，只要移除自己就行
		if (this.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE && worldPoint != null) {
			SpaceWorldPoint spacePoint = (SpaceWorldPoint) worldPoint;
			spacePoint.removeDefMarch(this);
			return;
		}
		
		// 如果是还没有到达的集结行军，要把所有参与者也遣返
		if (this.isMassMarch() && this.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			Set<IWorldMarch> tmpSet = WorldMarchService.getInstance().getMassJoinMarchs(this, false);
			for (IWorldMarch march : tmpSet) {
				// 自己会在外层被移除，这里不需要处理
				if (!march.getMarchId().equals(this.getMarchId())) {
					//march.onMarchReturn();
					WorldMarchService.getInstance().onMarchReturn(march, HawkTime.getMillisecond(), getMarchEntity().getAwardItems(), getMarchEntity().getArmys(), 0, 0);
				}
			}
		}
	}
	
	default void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		SpaceWorldPoint spacePoint = (SpaceWorldPoint) worldPoint;
		spacePoint.removeDefMarch(this);
		WorldMarchService.getInstance().onMarchReturn(this, callbackTime, getMarchEntity().getAwardItems(), getMarchEntity().getArmys(), 0, 0);
	}
	
}
