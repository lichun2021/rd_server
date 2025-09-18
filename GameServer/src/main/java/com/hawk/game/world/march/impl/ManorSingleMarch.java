package com.hawk.game.world.march.impl;

import java.util.Set;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.ManorMarch;

/**
 * 单人攻占联盟领地
 * 
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class ManorSingleMarch extends PlayerMarch implements ManorMarch, IReportPushMarch, IPassiveAlarmTriggerMarch {

	public ManorSingleMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.MANOR_SINGLE;
	}

	@Override
	public boolean needShowInGuildWar() {
		return this.isMarchState();
	}

	@Override
	public Set<String> attackReportRecipients() {
		return ReportRecipients.TargetManor.attackReportRecipients(this);
	}

	@Override
	public void onMarchReach(Player player) {
		ManorMarch.super.onMarchReach(player);
		// 删除行军报告
		this.removeAttackReport();
		this.pullAttackReport();
	}

	@Override
	public void onMarchStart() {
		this.pushAttackReport();
	}

	@Override
	public void onMarchReturn() {
		// 删除行军报告
		this.removeAttackReport();
		this.pullAttackReport();
	}

	@Override
	public void remove() {
		super.remove();
		// 删除行军报告
		this.removeAttackReport();
		this.pullAttackReport();
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
