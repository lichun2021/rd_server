package com.hawk.game.world.march.impl;

import java.util.Set;

import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.MassJoinMarch;
import com.hawk.game.world.march.submarch.MassMarch;
import com.hawk.game.world.march.submarch.PresidentMarch;

/**
 * 集结攻占总统府
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class PresidentMassMarch extends PlayerMarch implements MassMarch, PresidentMarch, IReportPushMarch, IPassiveAlarmTriggerMarch{

	public PresidentMassMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}
	
	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.PRESIDENT_MASS;
	}

	@Override
	public WorldMarchType getJoinMassType() {
		return WorldMarchType.PRESIDENT_MASS_JOIN;
	}
	
	@Override
	public boolean needShowInGuildWar() {
//		if (CrossActivityService.getInstance().isOpen()) {
//			return false;
//		}
		return true;
	}
	
	@Override
	public boolean needShowInNationWar() {
//		if (CrossActivityService.getInstance().isOpen()) {
//			return true;
//		}
		return false;
	}
	
	@Override
	public void onMarchReach(Player player) {
		PresidentMarch.super.onMarchReach(player);
		this.removeAttackReport();
		if (this.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {// 战胜
			this.pullAttackReport();
		} else {
			for (IWorldMarch targetMarch : alarmPointMarches()) {
				if (targetMarch.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
					continue;
				}
				if (targetMarch instanceof IReportPushMarch) {
					((IReportPushMarch) targetMarch).pushAttackReport();
				}
			}
		}
	}

	@Override
	public void teamMarchReached(MassJoinMarch teamMarch) {
		MassMarch.super.teamMarchReached(teamMarch);
		this.pushAttackReport();
	}

	@Override
	public void teamMarchCallBack(MassJoinMarch teamMarch) {
		MassMarch.super.teamMarchCallBack(teamMarch);
		this.pushAttackReport();
	}

	@Override
	public void onMarchStart() {
		PresidentMarch.super.onMarchStart();
		this.pushAttackReport();
	}

	@Override
	public void onMarchReturn() {
		PresidentMarch.super.onMarchReturn();
		// 删除行军报告
		this.removeAttackReport();
		this.removeAttackReportFromPoint(getOrigionX(), getOrigionY());
	}

	@Override
	public void remove() {
		super.remove();
		// 删除行军报告
		this.removeAttackReport();
		this.removeAttackReportFromPoint(getOrigionX(), getOrigionY());
	}

	@Override
	public boolean isEvident() {
		return IReportPushMarch.super.isEvident() || this.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
	}

	@Override
	public void pullAttackReport() {
		for (IWorldMarch targetMarch : alarmPointMarches()) {
			if (targetMarch instanceof IReportPushMarch) {
				((IReportPushMarch) targetMarch).pushAttackReport();
			}
		}
	}
	
	@Override
	public void pullAttackReport(String playerId) {
		for (IWorldMarch targetMarch : alarmPointMarches()) {
			if (targetMarch instanceof IReportPushMarch) {
				((IReportPushMarch) targetMarch).pushAttackReport(playerId);
			}
		}
	}

	@Override
	public Set<String> attackReportRecipients() {
		return ReportRecipients.TargetPresident.attackReportRecipients(this);
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
