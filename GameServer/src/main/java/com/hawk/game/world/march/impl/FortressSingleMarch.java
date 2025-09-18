package com.hawk.game.world.march.impl;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.FortressMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;

public class FortressSingleMarch extends PlayerMarch implements FortressMarch, IReportPushMarch, IPassiveAlarmTriggerMarch {

	public FortressSingleMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.FORTRESS_SINGLE;
	}

	@Override
	public boolean needShowInGuildWar() {
		if (WorldUtil.isReturnBackMarch(this)) {
			return false;
		}
		Player player = GlobalData.getInstance().makesurePlayer(this.getPlayerId());
		if (player == null || !player.hasGuild()) {
			return false;
		}
		
		Player fortressLeader = WorldMarchService.getInstance().getFortressLeader(this.getMarchEntity().getTerminalId());
		if (fortressLeader == null || !fortressLeader.hasGuild()) {
			return false;
		}
		return !player.getGuildId().equals(fortressLeader.getGuildId());
	}

	@Override
	public void onMarchReach(Player player) {
		FortressMarch.super.onMarchReach(player);
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
		List<IWorldMarch> defMarchs = WorldMarchService.getInstance().getFortressStayMarchs(getAlarmPointId());
		return defMarchs.stream()
				.map(IWorldMarch::getPlayerId)
				.filter(tid -> !Objects.equals(tid, getPlayerId()))
				.collect(Collectors.toSet());
	}

	@Override
	public void onMarchStart() {
		FortressMarch.super.onMarchStart();
		this.pushAttackReport();
		
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
