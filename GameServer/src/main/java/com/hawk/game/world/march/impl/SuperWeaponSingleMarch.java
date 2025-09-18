package com.hawk.game.world.march.impl;

import java.util.Set;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.superweapon.weapon.IWeapon;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.SuperWeaponMarch;

/**
 * 超级武器单人行军
 * @author golden
 *
 */
public class SuperWeaponSingleMarch  extends PlayerMarch implements SuperWeaponMarch, IReportPushMarch, IPassiveAlarmTriggerMarch {

	public SuperWeaponSingleMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.SUPER_WEAPON_SINGLE;
	}

	@Override
	public boolean needShowInGuildWar() {
		if (WorldUtil.isReturnBackMarch(this)) {
			return false;
		}
		Player player = GlobalData.getInstance().makesurePlayer(this.getPlayerId());
		IWeapon weapon = SuperWeaponService.getInstance().getWeapon(this.getMarchEntity().getTerminalId());
		return !player.getGuildId().equals(weapon.getGuildId());
	}
	
	@Override
	public void onMarchReach(Player player) {
		SuperWeaponMarch.super.onMarchReach(player);
		// 删除行军报告
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
	public Set<String> attackReportRecipients() {
		return ReportRecipients.TargetSuperWeapon.attackReportRecipients(this);
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
}
