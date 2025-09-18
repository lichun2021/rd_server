package com.hawk.game.module.dayazhizhan.battleroom.worldmarch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hawk.game.module.dayazhizhan.battleroom.IDYZZWorldPoint;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZMassMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZPassiveAlarmTriggerMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZReportPushMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.IDYZZBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;

/** 集结
 */
public class DYZZBuildingMarchMass extends IDYZZMassMarch implements IDYZZReportPushMarch , IDYZZPassiveAlarmTriggerMarch {

	private WorldMarchType marchType;
	private WorldMarchType joinMassType;

	public DYZZBuildingMarchMass(IDYZZPlayer parent) {
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
		IDYZZBuilding point = (IDYZZBuilding) getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
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
		IDYZZWorldPoint point = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
		IDYZZBuilding build = (IDYZZBuilding) point;

		return build.getGuildWarPassivityInfo();
	}

	@Override
	public boolean isMassMarch() {
		return true;
	}

	@Override
	public boolean isEvident() {
		return IDYZZReportPushMarch.super.isEvident() || this.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
	}

	@Override
	public Set<String> attackReportRecipients() {
		// 防守方援军
		List<IDYZZWorldMarch> helpMarchList = getParent().getParent().getPointMarches(getMarchEntity().getTerminalId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		Set<String> result = new HashSet<>();
		for (IDYZZWorldMarch march : helpMarchList) {
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
		List<IDYZZWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (IDYZZWorldMarch march : marchList) {
			if (march instanceof IDYZZReportPushMarch && march != this) {
				((IDYZZReportPushMarch) march).pushAttackReport();
			}
		}
	}

	@Override
	public void pullAttackReport(String playerId) {
		List<IDYZZWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (IDYZZWorldMarch march : marchList) {
			if (march instanceof IDYZZReportPushMarch && march != this) {
				((IDYZZReportPushMarch) march).pushAttackReport(playerId);
			}
		}
	}

}
