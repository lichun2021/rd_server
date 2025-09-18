package com.hawk.game.lianmengxzq.march;

import java.util.Objects;
import java.util.Set;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.lianmengxzq.worldpoint.XZQWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.GuildService;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.MassJoinMarch;
import com.hawk.game.world.march.submarch.MassMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 超级武器集结行军
 * @author golden
 *
 */
public class XZQMassMarch extends PlayerMarch implements MassMarch, IXZQMarch, IReportPushMarch, IPassiveAlarmTriggerMarch {

	public XZQMassMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.XZQ_MASS;
	}

	@Override
	public WorldMarchType getJoinMassType() {
		return WorldMarchType.XZQ_MASS_JOIN;
	}

	@Override
	public void register() {
		super.register();
		// 行军发起玩家id
		String fromPlayerId = this.getPlayerId();
		String fromGuildId = GuildService.getInstance().getPlayerGuildId(fromPlayerId);
		WorldMarchService.getInstance().addGuildMarch(fromGuildId, this.getMarchId());
		// 行军目标方联盟战争
		int terminalId = this.getMarchEntity().getTerminalId();
		WorldPoint terminaPoint = WorldPointService.getInstance().getWorldPoint(terminalId);
		XZQWorldPoint xzqWorldPoint = (XZQWorldPoint) terminaPoint;
		//当前攻破者
		Player occupyLeader = WorldMarchService.getInstance().getXZQLeader(xzqWorldPoint.getId());
		if (Objects.nonNull(occupyLeader)) {
			String toGuildId = occupyLeader.getGuildId();
			WorldMarchService.getInstance().addGuildMarch(toGuildId, this.getMarchId());
		}
		//当前控制者
		String controlGuild = xzqWorldPoint.getGuildControl();
		if (!HawkOSOperator.isEmptyString(controlGuild)) {
			WorldMarchService.getInstance().addGuildMarch(controlGuild, this.getMarchId());
		}
	}
	
	
	@Override
	public boolean needShowInGuildWar() {
		return true;
	}
	
	

	@Override
	public void onMarchReach(Player player) {
		IXZQMarch.super.onMarchReach(player);
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
		IXZQMarch.super.onMarchStart();
		this.pushAttackReport();
	}

	@Override
	public void onMarchReturn() {
		IXZQMarch.super.onMarchReturn();
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
		return ReportRecipients.TargetXzq.attackReportRecipients(this);
	}
}
