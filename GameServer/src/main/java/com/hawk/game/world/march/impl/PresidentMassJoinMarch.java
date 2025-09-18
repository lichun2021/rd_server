package com.hawk.game.world.march.impl;

import java.util.HashSet;
import java.util.Set;

import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.MassJoinMarch;
import com.hawk.game.world.march.submarch.PresidentMarch;

/**
 * 集结攻占总统府参与者
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class PresidentMassJoinMarch extends PassiveMarch implements MassJoinMarch, PresidentMarch,IReportPushMarch, IPassiveAlarmTriggerMarch{

	public PresidentMassJoinMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.PRESIDENT_MASS_JOIN;
	}

	@Override
	public void detailMarchStop(WorldPoint targetPoint) {
		MassJoinMarch.super.detailMarchStop(targetPoint);
		//集结加入行军, 只有在到达目标进行驻守的时候, 需要走manor的stop方法, 到达队长家的时候不需要执行
		if(this.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE){
			PresidentMarch.super.detailMarchStop(targetPoint);
		}
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
	public boolean isNationMassMarch() {
//		if (!CrossActivityService.getInstance().isOpen()) {
//			return false;
//		}
//		return true;
		return false;
	}
}
