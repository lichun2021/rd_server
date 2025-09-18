package com.hawk.game.world.march.impl;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.FortressMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.MassJoinMarch;
import com.hawk.game.world.march.submarch.MassMarch;

public class FortressMassMarch extends PlayerMarch implements MassMarch, FortressMarch , IReportPushMarch, IPassiveAlarmTriggerMarch{

	public FortressMassMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.FORTRESS_MASS;
	}

	@Override
	public void onMarchStart() {
		FortressMarch.super.onMarchStart();
		this.pushAttackReport();
	}
	
	@Override
	public void onMarchReach(Player player) {
		FortressMarch.super.onMarchReach(player);
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
	public boolean needShowInGuildWar() {
		return true;
	}
	
	@Override
	public WorldMarchType getJoinMassType() {
		return WorldMarchType.FORTRESS_JOIN;
	}
	
	@Override
	public void onMarchReturn() {
		FortressMarch.super.onMarchReturn();
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
		List<IWorldMarch> defMarchs = WorldMarchService.getInstance().getFortressStayMarchs(getAlarmPointId());
		return defMarchs.stream()
				.map(IWorldMarch::getPlayerId)
				.filter(tid -> !Objects.equals(tid, getPlayerId()))
				.collect(Collectors.toSet());
	}
}
