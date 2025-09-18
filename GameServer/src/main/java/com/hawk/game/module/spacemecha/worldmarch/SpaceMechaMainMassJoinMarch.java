package com.hawk.game.module.spacemecha.worldmarch;

import java.util.Collections;
import java.util.Set;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.MassJoinMarch;

/**
 * 联盟机甲主舱体守卫集结加入行军
 * 
 * @author lating
 *
 */
public class SpaceMechaMainMassJoinMarch extends PassiveMarch implements MassJoinMarch, MechaSpaceMarch,IReportPushMarch, IPassiveAlarmTriggerMarch {

	public SpaceMechaMainMassJoinMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_JOIN;
	}
	
	@Override
	public void onMarchStart() {
		this.pushAttackReport();
	}

	@Override
	public void onMarchReach(Player player) {
		MassJoinMarch.super.onMarchReach(player);
		leaderMarch().ifPresent(march -> march.teamMarchReached(this));
		//this.removeAttackReport();
	}
	
	@Override
	public void detailMarchStop(WorldPoint targetPoint) {
		MassJoinMarch.super.detailMarchStop(targetPoint);
		if(this.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE){
			MechaSpaceMarch.super.detailMarchStop(targetPoint);
		}
	}
	
	@Override
	public void onMarchReturn() {
		leaderMarch().ifPresent(march -> march.teamMarchCallBack(this));
		//this.removeAttackReport();
		this.removeAttackReportFromPoint(getOrigionX(), getOrigionY());
	}
	
	@Override
	public Set<String> attackReportRecipients() {
//		Set<String> allToNotify = new HashSet<>();
//		leaderMarch().ifPresent(march -> allToNotify.add(march.getMarchEntity().getPlayerId()));
//		return allToNotify;
		return Collections.emptySet();
	}
	
	@Override
	public void pullAttackReport() {
//		leaderMarch().ifPresent(leaderMarch -> {
//			if(leaderMarch instanceof IPassiveAlarmTriggerMarch){
//				((IPassiveAlarmTriggerMarch) leaderMarch).pullAttackReport();
//			}
//		});
	}
	
	@Override
	public void pullAttackReport(String playerId) {
//		leaderMarch().ifPresent(leaderMarch -> {
//			if(leaderMarch instanceof IPassiveAlarmTriggerMarch){
//				((IPassiveAlarmTriggerMarch) leaderMarch).pullAttackReport(playerId);
//			}
//		});
	}
	
	@Override
	public void remove() {
		super.remove();
		// 删除行军报告
		//this.removeAttackReport();
		this.removeAttackReportFromPoint(getTerminalX(), getTerminalY());
	}

	@Override
	public boolean isSpaceStateMatch() {
		return true;
	}

	@Override
	public boolean marchCountLimitCheck() {
		return true;
	}
	
	public boolean isSelfGuildSpace() {
		return true;
	}
	
	public boolean pointCheck() {
		return true;
	}

	@Override
	public void moveCityProcess(long currentTime) {
		MechaSpaceMarch.super.moveCityProcess(currentTime);
	}

	@Override
	public void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		MechaSpaceMarch.super.onMarchCallback(callbackTime, worldPoint);
	}
	
}
