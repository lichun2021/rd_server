package com.hawk.game.world.march.submarch;

import java.util.List;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.guild.manor.ManorMarchEnum;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.log.Action;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.log.Source;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 领地行军
 * @author zhenyu.shang
 * @since 2017年8月29日
 */
public interface ManorMarch extends BasedMarch {

	@Override
	default boolean isManorMarch() {
		return true;
	}

	@Override
	default void detailMarchStop(WorldPoint targetPoint) {
		WorldMarchService.getInstance().addManorMarchs(targetPoint.getId(), this, false);
		//处理行军到达
		ManorMarchEnum manorMarch = ManorMarchEnum.valueOf(this.getMarchType().getNumber()); // 获取行军枚举
		manorMarch.onMarchReach(targetPoint, this);
	}

	@Override
	default void onMarchReach(Player player) {
		// 集结和采集走自己的reach
		if (getMarchType() == WorldMarchType.MANOR_COLLECT || getMarchType() == WorldMarchType.MANOR_MASS_JOIN || getMarchType() == WorldMarchType.MANOR_ASSISTANCE_MASS_JOIN) {
			return;
		}
		WorldMarch worldMarch = getMarchEntity();
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(worldMarch.getTerminalId());
		
		List<IWorldMarch> marchList = getMassMarchList(this); // 统一按照集结行军处理
		
		if (worldPoint == null || !WorldUtil.isGuildBuildPoint(worldPoint)) { // 点类型错误，直接返回
			for (IWorldMarch iWorldMarch : marchList) {
				WorldMarchService.getInstance().onPlayerNoneAction(iWorldMarch, worldMarch.getReachTime());
			}
			return;
		}
		

		ManorMarchEnum manorMarch = ManorMarchEnum.valueOf(worldMarch.getMarchType()); // 获取行军枚举
		if (manorMarch == null) {
			returnMarchList(marchList);
			return;
		}

		int resCode = manorMarch.checkMarch(worldPoint, player, true); // 不同类型的行军各自检查
		if (resCode != Status.SysError.SUCCESS_OK_VALUE) {
			returnMarchList(marchList);
			return;
		}
		StringBuilder sb = new StringBuilder();
		if (WorldMarchService.getInstance().hasEnemyInPoint(worldMarch.getPlayerId(), worldPoint)) {
			sb.append("Enemy:true").append(",");
			boolean isAtkWin = attackWarPoint(marchList, worldPoint, player); // 战斗
			sb.append("isAtkWin:" + isAtkWin);
		} else {
			sb.append("Enemy:false").append(",");
			assitenceWarPoint(marchList, worldPoint, player); // 援助
			// 此处加判断是因为，如果在上个方法处理中，行军已经返回，则不需要执行到达方法
			if (!worldMarch.isInvalid() && worldMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				sb.append("Reach:true");
			} else {
				sb.append("Reach:false");
			}
		}
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MANOR_MARCH_REACH,
				Params.valueOf("marchType", worldMarch.getMarchType()),
				Params.valueOf("reachData", sb.toString()),
				Params.valueOf("marchData", worldMarch));
	}

	/**
	 * 获取被动方联盟战争界面信息
	 */
	@Override
	default GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		builder.setPointType(WorldPointType.GUILD_TERRITORY);
		builder.setX(this.getMarchEntity().getTerminalX());
		builder.setY(this.getMarchEntity().getTerminalY());
		// 建筑id
		String manorId = this.getMarchEntity().getTargetId();
		// 建筑
		int manorPostion = GuildManorService.getInstance().getManorPostion(manorId);

		// 建筑世界点
		WorldPoint buildPoint = WorldPointService.getInstance().getWorldPoint(manorPostion);
		if (buildPoint == null) {
			return builder;
		}

		GuildManorObj manor = GuildManorService.getInstance().getGuildManorByPoint(buildPoint);
		builder.setGuildBuildName(manor.getEntity().getManorName());

		// 队长
		Player leader = GuildManorService.getInstance().getManorLeader(buildPoint.getId());
		if (leader == null || !leader.getGuildId().equals(manor.getGuildId())) {
			return builder;
		}
		// 队长行军
		IWorldMarch manorLeaderMarch = GuildManorService.getInstance().getManorLeaderMarch(buildPoint.getId());
		if (manorLeaderMarch == null) {
			return builder;
		}

		builder.setLeaderMarch(getGuildWarSingleInfo(manorLeaderMarch.getMarchEntity()));
		
		// 已经到达的士兵数量
		int reachArmyCount = WorldUtil.calcSoldierCnt(manorLeaderMarch.getMarchEntity().getArmys());
		
		// 队员行军
		List<IWorldMarch> manorBuildMarchs = GuildManorService.getInstance().getManorBuildMarch(buildPoint.getId());
		for (IWorldMarch manorBuildMarch : manorBuildMarchs) {
			// 去除队长的
			if (manorLeaderMarch.getMarchId().equals(manorBuildMarch.getMarchId())) {
				continue;
			}
			builder.addJoinMarchs(getGuildWarSingleInfo(manorBuildMarch.getMarchEntity()));
			reachArmyCount += WorldUtil.calcSoldierCnt(manorBuildMarch.getMarchEntity().getArmys());
		}

		builder.setLeaderArmyLimit(manorLeaderMarch.getMaxMassJoinSoldierNum(leader));

		if (!HawkOSOperator.isEmptyString(leader.getGuildId())) {
			String guildTag = GuildService.getInstance().getGuildTag(leader.getGuildId());
			builder.setGuildTag(guildTag);
		}
		builder.setReachArmyCount(reachArmyCount);
		return builder;
	}
}
