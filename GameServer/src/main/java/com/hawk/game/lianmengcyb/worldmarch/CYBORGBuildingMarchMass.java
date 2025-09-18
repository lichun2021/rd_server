package com.hawk.game.lianmengcyb.worldmarch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hawk.game.lianmengcyb.ICYBORGWorldPoint;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldmarch.submarch.ICYBORGMassMarch;
import com.hawk.game.lianmengcyb.worldmarch.submarch.ICYBORGPassiveAlarmTriggerMarch;
import com.hawk.game.lianmengcyb.worldmarch.submarch.ICYBORGReportPushMarch;
import com.hawk.game.lianmengcyb.worldpoint.ICYBORGBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;

/** 集结
 */
public class CYBORGBuildingMarchMass extends ICYBORGMassMarch implements ICYBORGReportPushMarch , ICYBORGPassiveAlarmTriggerMarch {

	private WorldMarchType marchType;
	private WorldMarchType joinMassType;

	public CYBORGBuildingMarchMass(ICYBORGPlayer parent) {
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
		onArmyBack(getParent(), getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(), getMarchEntity().getSuperSoldierId(), this);

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
		ICYBORGBuilding point = (ICYBORGBuilding) getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
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
		ICYBORGWorldPoint point = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
		ICYBORGBuilding build = (ICYBORGBuilding) point;

		return build.getGuildWarPassivityInfo();
	}

	@Override
	public boolean isMassMarch() {
		return true;
	}

	@Override
	public boolean isEvident() {
		return ICYBORGReportPushMarch.super.isEvident() || this.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
	}

	@Override
	public Set<String> attackReportRecipients() {
		// 防守方援军
		List<ICYBORGWorldMarch> helpMarchList = getParent().getParent().getPointMarches(getMarchEntity().getTerminalId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		Set<String> result = new HashSet<>();
		for (ICYBORGWorldMarch march : helpMarchList) {
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
		List<ICYBORGWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (ICYBORGWorldMarch march : marchList) {
			if (march instanceof ICYBORGReportPushMarch && march != this) {
				((ICYBORGReportPushMarch) march).pushAttackReport();
			}
		}
	}

	@Override
	public void pullAttackReport(String playerId) {
		List<ICYBORGWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (ICYBORGWorldMarch march : marchList) {
			if (march instanceof ICYBORGReportPushMarch && march != this) {
				((ICYBORGReportPushMarch) march).pushAttackReport(playerId);
			}
		}
	}

}
