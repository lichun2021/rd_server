package com.hawk.game.world.march.submarch;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.EnterPresidentEvent;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.president.PresidentCity;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 国王战行军
 * @author golden
 *
 */
public interface PresidentMarch extends BasedMarch {
	
	@Override
	default boolean isPresidentMarch() {
		return true;
	}

	@Override
	default void onMarchReach(Player player) {
		// 统一按多人一起处理
		List<IWorldMarch> massMarchList = getMassMarchList(this);
		// 活动时间段判断
		if (!PresidentFightService.getInstance().isFightPeriod()) {
			doPeriodChange(massMarchList);
			return;
		}
		
		// 玩家联盟判断
		if (!player.hasGuild()) {
			returnMarchList(massMarchList);
			return;
		}
		
		// 当前占领联盟
		String guildId = PresidentFightService.getInstance().getCurrentGuildId();
		
//		// 跨服王战开启
//		if (CrossActivityService.getInstance().isOpen()) {
//			String occupyServer = PresidentFightService.getInstance().getCurrentServerId();
//			if (!HawkOSOperator.isEmptyString(occupyServer)) {
//				boolean isSameCamp = GuildService.getInstance().isSameCamp(player, guildId);
//				boolean isSameServer = this.getPlayer().getMainServerId().equals(occupyServer);
//				boolean hasMarchBefore = WorldMarchService.getInstance().hasPresidentMarch();
//				// 如果是同阵营但是是不同服的,则行军返回
//				if (hasMarchBefore && isSameCamp && !isSameServer) {
//					returnMarchList(massMarchList);
//					return;
//				}
//			}
//		}
		
		Set<String> atkPlayerIds = new HashSet<>();
		for (IWorldMarch march : massMarchList) {
			atkPlayerIds.add(march.getPlayerId());
		}
		
		// 王城点
		WorldPoint worldPoint = WorldPointService.getInstance().getPresidentPoint();
		
		// 本 盟/服 占领，走援助逻辑
		if (!HawkOSOperator.isEmptyString(guildId) && guildId.equals(player.getGuildId())) {
			// 添加援助战争记录
			PresidentFightService.getInstance().doPresidentAssistance(this);
			// 援助战斗点
			assitenceWarPoint(massMarchList, worldPoint, player);
			
			for (String playerId : atkPlayerIds) {
				ActivityManager.getInstance().postEvent(new EnterPresidentEvent(playerId));
			}
			
		} else {
			// 王城里是否有行军
			boolean hasMarchBefore = WorldMarchService.getInstance().hasPresidentMarch();
			// 有行军驻扎走战斗逻辑， 没有则直接驻扎
			if (hasMarchBefore) {
				boolean atkWin = attackWarPoint(massMarchList, worldPoint, player);
				if (atkWin) {
					for (String playerId : atkPlayerIds) {
						ActivityManager.getInstance().postEvent(new EnterPresidentEvent(playerId));
					}
					CrossActivityService.getInstance().occupyPresident(player.getGuildId(), atkPlayerIds);
				}
			} else {
				PresidentFightService.getInstance().doPresidentAttackWin(this.getPlayerId(), null);
				for (IWorldMarch march : massMarchList) {
					march.onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE, march.getMarchEntity().getArmys(), worldPoint);
				}
				for (String playerId : atkPlayerIds) {
					ActivityManager.getInstance().postEvent(new EnterPresidentEvent(playerId));
				}
				CrossActivityService.getInstance().occupyPresident(player.getGuildId(), atkPlayerIds);
			}
		}
		
		// 通知场景本点数据更新
		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
	}
	
	@Override
	default void detailMarchStop(WorldPoint targetPoint) {
		
		// 之前没有行军占领，刷新联盟战争显示
		if (!WorldMarchService.getInstance().hasPresidentMarch()) {
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
		
		WorldMarchService.getInstance().addPresidentMarch(this, false);
		LogUtil.logCrossActivtyOccupyPresident(getPlayer(), getPlayer().getGuildId());
	}

	/**
	 * 总统府状态变更处理 : 发邮件, 并返回行军
	 * @param massMarchList
	 */
	default void doPeriodChange(List<IWorldMarch> massMarchList) {
//		for (IWorldMarch worldMarch : massMarchList) {
//			FightMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(worldMarch.getPlayerId()).setMailId(MailId.ATTACK_CAPITAL_FAILED_CMBAT_FINISHED).build());
//		}
		returnMarchList(massMarchList);
	}

	/**
	 * 获取被动方联盟战争界面信息
	 */
	default GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		
		WorldPoint presidentPoint = WorldPointService.getInstance().getPresidentPoint();
		builder.setPointType(WorldPointType.KING_PALACE);
		builder.setX(presidentPoint.getX());
		builder.setY(presidentPoint.getY());

		// 队长id
		Player leader = WorldMarchService.getInstance().getPresidentLeader();
		if (leader == null) {
			
			try {
				if (CrossActivityService.getInstance().isOpen()) {
					PresidentCity presidentCity = PresidentFightService.getInstance().getPresidentCity();
					String guildId = presidentCity.getGuildId();
					if (!HawkOSOperator.isEmptyString(guildId)) {
						String guildTag = GuildService.getInstance().getGuildTag(guildId);
						builder.setGuildTag(guildTag);
						builder.setServerId(GuildService.getInstance().getGuildServerId(guildId));
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			return builder;
		}
		
		builder.setGridCount(leader.getMaxMassJoinMarchNum(this));
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
		String presidentLeaderMarchId = WorldMarchService.getInstance().getPresidentLeaderMarch();
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(presidentLeaderMarchId);
		for (ArmyInfo army : leaderMarch.getMarchEntity().getArmys()) {
			leaderInfo.addArmys(army.toArmySoldierPB(leader));
		}
		leaderInfo.setMarchId(presidentLeaderMarchId);
		builder.setLeaderMarch(leaderInfo);
		// 已经到达的士兵数量
		int reachArmyCount = WorldUtil.calcSoldierCnt(leaderMarch.getMarchEntity().getArmys());
		
		List<IWorldMarch> marchs = WorldMarchService.getInstance().getPresidentQuarteredMarchs();
		for (IWorldMarch stayMarch : marchs) {
			if (stayMarch.getMarchId().equals(presidentLeaderMarchId)) {
				continue;
			}
			builder.addJoinMarchs(getGuildWarSingleInfo(stayMarch.getMarchEntity()));
			reachArmyCount += WorldUtil.calcSoldierCnt(stayMarch.getMarchEntity().getArmys());
		}
		builder.setLeaderArmyLimit(leaderMarch.getMaxMassJoinSoldierNum(leader));
		builder.setReachArmyCount(reachArmyCount);
		builder.setServerId(leader.getMainServerId());
		return builder;
	}
	
	@Override
	public default Set<IWorldMarch> getQuarterMarch() {
		Set<IWorldMarch> retMarchs = new HashSet<>();
		
		if (this.isReachAndStopMarch()) {
			BlockingDeque<String> presidentMarchs = WorldMarchService.getInstance().getPresidentMarchs();
			for (String marchId : presidentMarchs) {
				retMarchs.add(WorldMarchService.getInstance().getMarch(marchId));
			}
		}
		return retMarchs;
	}
	
	/**
	 * 是否需要显示在国家战争中
	 * @param tarPlayer
	 */
	@Override
	public default boolean needShowInNation(Player observer) {
//		try {
//			if (isReturnBackMarch()) {
//				return false;
//			}
//			// 观察者的服务器id
//			String observerServer = observer.getMainServerId();
//			// 行军的服务器id
//			String marchServer = getPlayer().getMainServerId();
//			// 王城当前占领的服务器id
//			String pointServer = PresidentFightService.getInstance().getPresidentCity().getServerId();
//			
//			// 自己服发起的行军
//			if (marchServer.equals(observerServer)) {
//				
//				// 单人行军
//				if (!this.isMassMarch()) {
//					if (!HawkOSOperator.isEmptyString(pointServer) && pointServer.equals(observerServer)) {
//						return false;
//					}
//				}
//				return true;
//			}
//			
//			int camp1 = CrossActivityService.getInstance().getCamp(observerServer);
//			int camp2 = CrossActivityService.getInstance().getCamp(marchServer);
//			
//			// 自己同阵营发起的,不显示
//			if (camp1 == camp2) {
//				return false;
//			}
//			
//			// 敌方集结的目标是自己
//			if (!HawkOSOperator.isEmptyString(pointServer) && pointServer.equals(observerServer)) {
//				return true;
//			} else {
//				return false;
//			}
//		} catch (Exception e) {
//			HawkException.catchException(e);
//		}
		return false;
	}
}
