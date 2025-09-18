package com.hawk.game.world.march.impl;

import java.util.HashSet;
import java.util.Set;

import org.hawk.os.HawkTime;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventOccupyPresidentTower;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.MassJoinMarch;
import com.hawk.game.world.march.submarch.PresidentTowerMarch;

/**
 * 国王战箭塔集结加入行军
 * @author golden
 *
 */
public class PresidentTowerMassJoinMarch extends PassiveMarch implements MassJoinMarch, PresidentTowerMarch,IReportPushMarch, IPassiveAlarmTriggerMarch {

	public PresidentTowerMassJoinMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}
	
	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.PRESIDENT_TOWER_MASS_JOIN;
	}
	
	@Override
	public void onMarchStart() {
		this.pushAttackReport();
	}

	@Override
	public void onMarchReach(Player player) {
		MassJoinMarch.super.onMarchReach(player);
		leaderMarch().ifPresent(march -> march.teamMarchReached(this));
		this.removeAttackReport();
	}

	@Override
	public void detailMarchStop(WorldPoint targetPoint) {
		MassJoinMarch.super.detailMarchStop(targetPoint);
		//集结加入行军, 只有在到达目标进行驻守的时候, 需要走manor的stop方法, 到达队长家的时候不需要执行
		if(this.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE){
			PresidentTowerMarch.super.detailMarchStop(targetPoint);
		}
	}
	
	@Override
	public void onMarchReturn() {
		leaderMarch().ifPresent(march -> march.teamMarchCallBack(this));
		this.removeAttackReport();
		this.removeAttackReportFromPoint(getOrigionX(), getOrigionY());
		pushEnvent();
	}
	
	@Override
	public void remove() {
		super.remove();
		// 删除行军报告
		this.removeAttackReport();
		this.removeAttackReportFromPoint(getOrigionX(), getOrigionY());
	}
	
	@Override
	public Set<String> attackReportRecipients() {
		Set<String> allToNotify = new HashSet<>();
		leaderMarch().ifPresent(march -> allToNotify.add(march.getMarchEntity().getPlayerId()));
		return allToNotify;
	}
	
	@Override
	public void pullAttackReport() {
		leaderMarch().ifPresent(leaderMarch -> {
			if(leaderMarch instanceof IPassiveAlarmTriggerMarch){
				((IPassiveAlarmTriggerMarch) leaderMarch).pullAttackReport();
			}
		});
	}
	
	@Override
	public void pullAttackReport(String playerId) {
		leaderMarch().ifPresent(leaderMarch -> {
			if(leaderMarch instanceof IPassiveAlarmTriggerMarch){
				((IPassiveAlarmTriggerMarch) leaderMarch).pullAttackReport(playerId);
			}
		});
	}
	
	@Override
	public boolean beforeImmediatelyRemoveMarchProcess(Player player) {
		pushEnvent();
		return true;
	}
	
	public void pushEnvent() {
		if (this.getMarchEntity().getReachTime() == 0L) {
			return;
		}
		
		// 用 attackTimes 做标记 之前是否有推送过
		boolean pushEventBefore = (this.getMarchEntity().getAttackTimes() == 1);
		if (pushEventBefore) {
			return;
		}
		this.getMarchEntity().setAttackTimes(1);
		
		MissionManager.getInstance().postMsg(getPlayer(), new EventOccupyPresidentTower(HawkTime.getMillisecond() - this.getMarchEntity().getReachTime()));
	}
}
