package com.hawk.game.world.march.submarch;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 总统府箭塔行军
 * @author golden
 *
 */
public interface PresidentTowerMarch extends BasedMarch {
	
	@Override
	default boolean isPresidentTowerMarch() {
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
		
		// 目标点
		int terminalId = this.getMarchEntity().getTerminalId();
		// 当前占领联盟
		String guildId = PresidentFightService.getInstance().getPresidentTowerGuild(terminalId);
		
		// 王城箭塔
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(terminalId);
		HawkLog.logPrintln("PresidentTowerMarch onMarchReach player:{},terminalId:{},curGuild:{}", player.getId(),terminalId, guildId);
		
		// 本盟占领，走援助逻辑 它盟占领，走攻击逻辑
		if (!HawkOSOperator.isEmptyString(guildId) && player.getGuildId().equals(guildId)) {
			// 援助成功处理
			PresidentFightService.getInstance().doPresidentTowerAssistance(this, terminalId);
			// 援助战斗点
			assitenceWarPoint(massMarchList, worldPoint, player);
		} else {
			// 王城里是否有行军
			boolean hasMarchBefore = WorldMarchService.getInstance().hasPresidentTowerMarch(terminalId);
			// 有行军驻扎走战斗逻辑， 没有则直接驻扎
			if (hasMarchBefore) {
				attackWarPoint(massMarchList, worldPoint, player);
			} else {
				PresidentFightService.getInstance().doPresidentTowerAttackWin(this.getPlayerId(), null, terminalId);
				for (IWorldMarch march : massMarchList) {
					march.onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE, march.getMarchEntity().getArmys(), worldPoint);
				}
			}
		}
		
		// 通知场景本点数据更新
		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
	}
	
	@Override
	default void detailMarchStop(WorldPoint targetPoint) {
		// 之前没有行军占领，刷新联盟战争显示
		if (!WorldMarchService.getInstance().hasPresidentTowerMarch(targetPoint.getId())) {
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
		
		WorldMarchService.getInstance().addPresidentTowerMarch(targetPoint.getId(), this, false);
	}
	
	/**
	 * 总统府状态变更处理 : 发邮件, 并返回行军
	 * @param massMarchList
	 */
	default void doPeriodChange(List<IWorldMarch> massMarchList) {
//		for (IWorldMarch worldMarch : massMarchList) {
//			FightMailService.getInstance().sendMail(MailParames.newBuilder() .setPlayerId(worldMarch.getPlayerId()) .setMailId(MailId.ATTACK_CAPITAL_FAILED_CMBAT_FINISHED).build());
//		}
		returnMarchList(massMarchList);
	}
	
	/**
	 * 获取被动方联盟战争界面信息
	 */
	default GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		
		int towerPointId = this.getMarchEntity().getTerminalId();
		WorldPoint towerPoint = WorldPointService.getInstance().getWorldPoint(towerPointId);
		builder.setPointType(WorldPointType.CAPITAL_TOWER);
		builder.setX(towerPoint.getX());
		builder.setY(towerPoint.getY());

		// 队长id
		Player leader = WorldMarchService.getInstance().getPresidentTowerLeader(towerPointId);
		if (leader == null) {
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
		String presidentLeaderMarchId = WorldMarchService.getInstance().getPresidentTowerLeaderMarchId(towerPointId);
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(presidentLeaderMarchId);
		for (ArmyInfo army : leaderMarch.getMarchEntity().getArmys()) {
			leaderInfo.addArmys(army.toArmySoldierPB(leader));
		}
		leaderInfo.setMarchId(presidentLeaderMarchId);
		builder.setLeaderMarch(leaderInfo);
		
		// 已经到达的士兵数量
		int reachArmyCount = WorldUtil.calcSoldierCnt(leaderMarch.getMarchEntity().getArmys());
		
		List<IWorldMarch> marchs = WorldMarchService.getInstance().getPresidentTowerStayMarchs(towerPointId);
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
			BlockingDeque<String> presidentMarchs = WorldMarchService.getInstance().getPresidentTowerMarchs(this.getTerminalId());
			for (String marchId : presidentMarchs) {
				retMarchs.add(WorldMarchService.getInstance().getMarch(marchId));
			}
		}
		return retMarchs;
	}
}
