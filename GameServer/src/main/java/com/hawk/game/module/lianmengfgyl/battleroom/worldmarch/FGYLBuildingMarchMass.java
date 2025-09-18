package com.hawk.game.module.lianmengfgyl.battleroom.worldmarch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hawk.game.module.lianmengfgyl.battleroom.IFGYLWorldPoint;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.submarch.IFGYLMassMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.submarch.IFGYLPassiveAlarmTriggerMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.submarch.IFGYLReportPushMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.IFGYLBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;

/** 集结
 */
public class FGYLBuildingMarchMass extends IFGYLMassMarch implements IFGYLReportPushMarch , IFGYLPassiveAlarmTriggerMarch {

	private WorldMarchType marchType;
	private WorldMarchType joinMassType;

	public FGYLBuildingMarchMass(IFGYLPlayer parent) {
		super(parent);
	}

	@Override
	public void heartBeats() {
		
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			return;
		}
		
		// 集结等待中
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			waitingStatusMarchProcess();
			return;
		}
		// 当前时间
		long currTime = getParent().getParent().getCurTimeMil();
		// 行军或者回程时间未结束
		if (getMarchEntity().getEndTime() > currTime) {
			return;
		}
		// 行军返回到达
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			onMarchBack();
			return;
		}

		// 行军到达
		onMarchReach(getParent());

	}

	@Override
	public void onMarchBack() {
		// 部队回城
		onArmyBack();

		this.remove();

	}

	@Override
	public WorldMarchType getMarchType() {
		return marchType;
	}

	@Override
	public WorldMarchType getJoinMassType() {
		return joinMassType;
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
	public void onMarchReach(Player player) {
		// 删除行军报告
		removeAttackReport();
		// 目标点
		IFGYLBuilding point = (IFGYLBuilding) getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
		point.onMarchReach(this);
		
		this.pullAttackReport();
	}

	@Override
	public boolean needShowInGuildWar() {
		return true;
	}

	/** 获取被动方联盟战争界面信息 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		IFGYLWorldPoint point = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
		IFGYLBuilding build = (IFGYLBuilding) point;

		return build.getGuildWarPassivityInfo();
	}

	@Override
	public boolean isMassMarch() {
		return true;
	}

	@Override
	public boolean isEvident() {
		return IFGYLReportPushMarch.super.isEvident() || this.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
	}

	@Override
	public Set<String> attackReportRecipients() {
		// 防守方援军
		List<IFGYLWorldMarch> helpMarchList = getParent().getParent().getPointMarches(getMarchEntity().getTerminalId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		Set<String> result = new HashSet<>();
		for (IFGYLWorldMarch march : helpMarchList) {
			result.add(march.getPlayerId());
		}
		return result;
	}

	public void setMarchType(WorldMarchType marchType) {
		this.marchType = marchType;
	}

	public void setJoinMassType(WorldMarchType joinMassType) {
		this.joinMassType = joinMassType;
	}

	@Override
	public void pullAttackReport() {
		List<IFGYLWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (IFGYLWorldMarch march : marchList) {
			if (march instanceof IFGYLReportPushMarch && march != this) {
				((IFGYLReportPushMarch) march).pushAttackReport();
			}
		}
	}

	@Override
	public void pullAttackReport(String playerId) {
		List<IFGYLWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (IFGYLWorldMarch march : marchList) {
			if (march instanceof IFGYLReportPushMarch && march != this) {
				((IFGYLReportPushMarch) march).pushAttackReport(playerId);
			}
		}
	}

}
