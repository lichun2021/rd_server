package com.hawk.game.world.march.impl;

import java.util.Set;

import org.hawk.os.HawkTime;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventOccupyPresidentTower;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.PresidentTowerMarch;

/**
 * 国王战箭塔单人行军
 * 
 * @author golden
 *
 */
public class PresidentTowerSingleMarch extends PlayerMarch implements PresidentTowerMarch, IReportPushMarch, IPassiveAlarmTriggerMarch {

	public PresidentTowerSingleMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.PRESIDENT_TOWER_SINGLE;
	}

	@Override
	public boolean needShowInGuildWar() {
		Player player = GlobalData.getInstance().makesurePlayer(this.getPlayerId());
		String presidentTowerGuild = PresidentFightService.getInstance().getPresidentTowerGuild(this.getMarchEntity().getTerminalId());
		return !player.getGuildId().equals(presidentTowerGuild);
	}

	@Override
	public void onMarchReach(Player player) {
		PresidentTowerMarch.super.onMarchReach(player);
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
		return ReportRecipients.TargetPresidentTower.attackReportRecipients(this);
	}

	@Override
	public void onMarchStart() {
		PresidentTowerMarch.super.onMarchStart();
		this.pushAttackReport();
	}

	@Override
	public void onMarchReturn() {
		PresidentTowerMarch.super.onMarchReturn();
		// 删除行军报告
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
