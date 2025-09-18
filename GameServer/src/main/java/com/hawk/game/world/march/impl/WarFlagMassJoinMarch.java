package com.hawk.game.world.march.impl;

import java.util.HashSet;
import java.util.Set;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.FlagMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.MassJoinMarch;

public class WarFlagMassJoinMarch extends PassiveMarch implements MassJoinMarch, FlagMarch,IReportPushMarch, IPassiveAlarmTriggerMarch {

	public WarFlagMassJoinMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.WAR_FLAG_MASS_JOIN;
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
	public void onMarchReturn() {
		FlagMarch.super.onMarchReturn();
		leaderMarch().ifPresent(march -> march.teamMarchCallBack(this));
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
	public void remove() {
		super.remove();
		// 删除行军报告
		this.removeAttackReport();
		this.removeAttackReportFromPoint(getOrigionX(), getOrigionY());
	}
	
	@Override
	public void detailMarchStop(WorldPoint targetPoint) {
		MassJoinMarch.super.detailMarchStop(targetPoint);
		if(this.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE){
			FlagMarch.super.detailMarchStop(targetPoint);
		}
	}
	
	@Override
	public void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		if (getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE
				|| getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
			MassJoinMarch.super.onMarchCallback(callbackTime, worldPoint);
		} else {
			FlagMarch.super.onMarchCallback(callbackTime, worldPoint);
		}
	}
	
	@Override
	public void moveCityProcess(long currentTime) {
		if (getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE
				|| getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE
				|| getMarchStatus() == WorldMarchStatus.MARCH_STATUS_JOIN_MARCH_VALUE) {
			MassJoinMarch.super.moveCityProcess(currentTime);
			
		} else {
			FlagMarch.super.moveCityProcess(currentTime);
		}
	}
}
