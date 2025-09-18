package com.hawk.game.world.march.submarch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkTime;

import com.hawk.game.global.GlobalData;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.log.Action;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.log.Source;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.autologic.service.GuildAutoMarchService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 集结加入行军
 * 
 * @author zhenyu.shang
 * @since 2017年8月26日
 */
public interface MassJoinMarch extends BasedMarch {

	@Override
	default boolean isMassJoinMarch() {
		return true;
	}
	
	/**
	 * 取得队长行军
	 * 
	 * @return
	 */
	default Optional<MassMarch> leaderMarch() {
		// 检查队长行军是否出发
		IWorldMarch massMarch = WorldMarchService.getInstance().getMarch(getMarchEntity().getTargetId());
		if (massMarch instanceof MassMarch) {

			return Optional.ofNullable((MassMarch) massMarch);
		}
		return Optional.empty();
	}

	@Override
	default void detailMarchStop(WorldPoint targetPoint) {
		WorldMarch march = getMarchEntity();
		if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			WorldMarchService.logger.info("world march stop mass join, marchData: {}", this);

			IWorldMarch massMarch = WorldMarchService.getInstance().getMarch(march.getTargetId());
			if (massMarch != null) {
				march.setStartTime(massMarch.getMarchEntity().getStartTime()); // 行军的开始时间和队长进行同步
				march.setEndTime(massMarch.getMarchEntity().getEndTime()); // 行军的结束时间和队长进行同步
				march.setMassReadyTime(massMarch.getMarchEntity().getMassReadyTime()); // 同步队长的准备时间
				march.setManorMarchReachTime(HawkApp.getInstance().getCurrentTime()); // 到达队长家的时间
			}
		}
	}

	@Override
	default void onMarchReach(Player player) {
		WorldMarch march = getMarchEntity();
		// 点是否已更新
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
		if (targetPoint == null) {
			WorldMarchService.getInstance().onMarchReturn(this, march.getArmys(), 0);
			return;
		}
		if (targetPoint == null || targetPoint.getPointType() != WorldPointType.PLAYER_VALUE) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			GuildMailService.getInstance()
					.sendMail(MailParames.newBuilder().setPlayerId(player.getId())
							.setMailId(MailId.MASS_FAILED_MEME_MOVED_CITY)
							.addContents(march.getTerminalX(), march.getTerminalY()).build());
			return;
		}

		// 检查队长行军是否出发
		String marchId = march.getTargetId();
		IWorldMarch massMarch = WorldMarchService.getInstance().getPlayerMarch(targetPoint.getPlayerId(), marchId);
		if (massMarch == null || massMarch.getMarchEntity().getStartTime() < HawkTime.getMillisecond()) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			return;
		}
		// 检查集结队伍是否超编
		Player leader = GlobalData.getInstance().makesurePlayer(massMarch.getPlayerId());
		Set<IWorldMarch> massMarchList = WorldMarchService.getInstance().getMassJoinMarchs(massMarch, true);
		int count = massMarchList != null ? massMarchList.size() : 0;
		if (count > leader.getMaxMassJoinMarchNum(massMarch) + massMarch.getMarchEntity().getBuyItemTimes()) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			WorldMarchService.getInstance().onMonsterRelatedMarchAction(this);
			return;
		}
		// 检查集结士兵数是否超上限
		int maxMassSoldierNum = massMarch.getMaxMassJoinSoldierNum(leader, player);
		// 队长现在带的兵力人口
		int curPopulationCnt = WorldUtil.calcSoldierCnt(massMarch.getMarchEntity().getArmys());
		if (massMarchList != null && massMarchList.size() > 0) {
			for (IWorldMarch joinMarch : massMarchList) {
				if (joinMarch.getPlayerId().equals(march.getPlayerId())) {
					continue;
				}
				curPopulationCnt += WorldUtil.calcSoldierCnt(joinMarch.getMarchEntity().getArmys());
			}
		}
		// 士兵人口数已达上限
		if (curPopulationCnt >= maxMassSoldierNum) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			WorldMarchService.getInstance().onMonsterRelatedMarchAction(this);
			return;
		}
		// 计算剩余兵力空位
		int remainSpace = maxMassSoldierNum - curPopulationCnt;
		List<ArmyInfo> stayList = new ArrayList<ArmyInfo>();
		List<ArmyInfo> backList = new ArrayList<ArmyInfo>();
		WorldUtil.calcStayArmy(march, remainSpace, stayList, backList);

		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_REACH_MASS_JOIN_REACH,
				Params.valueOf("march", march), Params.valueOf("remainSpace", remainSpace),
				Params.valueOf("stayList", stayList), Params.valueOf("backList", backList));
		// 回家的士兵生成新行军
		if (backList.size() > 0) {
			WorldMarchService.getInstance().onNewMarchReturn(player, this, backList, new ArrayList<>(), 0, null);
		}
		//集结加入行军，在行军到达队长家是，需要更新点信息
		WorldMarchService.getInstance().updatePointMarchInfo(this, true);
		
		this.onMarchStop(WorldMarchStatus.MARCH_STATUS_WAITING_VALUE, stayList, targetPoint);
	}

	@Override
	default void updateMarch() {
		if (this.getMarchEntity().isInvalid()) {
			return;
		}
		
		// 获取集结的队长行军
		IWorldMarch massMarch = WorldMarchService.getInstance().getMarch(this.getMarchEntity().getTargetId());
		if (massMarch != null) {
			WorldMarchService.getInstance().broadcastMassMarch2Team(massMarch);
		}
		
		BasedMarch.super.updateMarch();
	}
	
	
	@Override
	default void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		BasedMarch.super.onMarchCallback(callbackTime, worldPoint);
		
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(getMarchEntity().getTargetId());
		if (leaderMarch != null && leaderMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			// 发邮件---发车前有人主动离开
			int icon = GuildService.getInstance().getGuildFlagByPlayerId(getPlayerId());
			Set<IWorldMarch> marchers = WorldMarchService.getInstance().getMassJoinMarchs(leaderMarch, false);
			if (marchers == null) {
				marchers = new HashSet<IWorldMarch>();
			}
			marchers.add(this);
			marchers.add(leaderMarch);
			
			String name = GlobalData.getInstance().getPlayerNameById(getPlayerId());
			
			for (IWorldMarch worldMarch : marchers) {
				GuildMailService.getInstance()
						.sendMail(MailParames.newBuilder().setPlayerId(worldMarch.getPlayerId())
								.setMailId(MailId.MASS_PLAYER_LEAVE).addSubTitles(name)
								.addContents(name).setIcon(icon).build());
			}
		}
	}
	
	@Override
	default void moveCityProcess(long currentTime) {
		WorldMarchService.getInstance().massJoinPlayerMarchMoveCityProcess(this, currentTime);
	}
	
	
	
	default void autoMassJoinStop(){
		int auto = this.getMarchEntity().getAutoMassJoinIdentify();
		if(auto <= 0){
			return;
		}
		GuildAutoMarchService.getInstance().onAutoMassJoin(this.getPlayer());
		
		//日志
		IWorldMarch massMarch = WorldMarchService.getInstance().getMarch(this.getMarchEntity().getTargetId());
		if(Objects.nonNull(massMarch)){
			LogUtil.logAutoMassJionSuc(this.getPlayer(), this.getMarchId(), this.getMarchType().getNumber(), massMarch.getMarchId(), massMarch.getMarchType().getNumber());
		}
		
	}
}
