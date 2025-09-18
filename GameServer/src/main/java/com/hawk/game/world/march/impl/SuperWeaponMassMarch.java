package com.hawk.game.world.march.impl;

import java.util.Set;

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
import com.hawk.game.world.march.submarch.SuperWeaponMarch;

/**
 * 超级武器集结行军
 * @author golden
 *
 */
public class SuperWeaponMassMarch extends PlayerMarch implements MassMarch, SuperWeaponMarch, IReportPushMarch, IPassiveAlarmTriggerMarch{

	public SuperWeaponMassMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.SUPER_WEAPON_MASS;
	}

	@Override
	public WorldMarchType getJoinMassType() {
		return WorldMarchType.SUPER_WEAPON_MASS_JOIN;
	}

	@Override
	public boolean needShowInGuildWar() {
		return true;
	}
	
	@Override
	public void onMarchReach(Player player) {
		SuperWeaponMarch.super.onMarchReach(player);
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
		SuperWeaponMarch.super.onMarchStart();
		this.pushAttackReport();
	}

	@Override
	public void onMarchReturn() {
		SuperWeaponMarch.super.onMarchReturn();
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
		return ReportRecipients.TargetSuperWeapon.attackReportRecipients(this);
	}
}
