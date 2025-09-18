package com.hawk.game.lianmengcyb.worldmarch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldmarch.submarch.ICYBORGMassJoinMarch;
import com.hawk.game.lianmengcyb.worldmarch.submarch.ICYBORGPassiveAlarmTriggerMarch;
import com.hawk.game.lianmengcyb.worldmarch.submarch.ICYBORGReportPushMarch;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;

public class CYBORGBuildingMarchMassJoin extends ICYBORGMassJoinMarch implements ICYBORGReportPushMarch, ICYBORGPassiveAlarmTriggerMarch {

	public CYBORGBuildingMarchMassJoin(ICYBORGPlayer parent) {
		super(parent);
	}

	@Override
	public void heartBeats() {
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			return;
		}
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_JOIN_MARCH_VALUE) {
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
	public void onMarchStart() {
		this.pushAttackReport();
	}

	@Override
	public void onMarchReturn() {
		leaderMarch().ifPresent(march -> march.teamMarchCallBack(this));
		this.removeAttackReport();
		this.pullAttackReport();
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.valueOf(getMarchEntity().getMarchType());
	}

	@Override
	public void onMarchReach(Player parent) {
		super.onMarchReach(parent);
		leaderMarch().ifPresent(march -> march.teamMarchReached(this));
		notifyMarchEvent(MarchEvent.MARCH_DELETE);

		this.removeAttackReport();
	}

	/*** 返回家中 */
	public void onMarchBack() {
		// 部队回城
		onArmyBack(getParent(), getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(), getMarchEntity().getSuperSoldierId(), this);

		this.remove();

	}

	@Override
	public Set<String> attackReportRecipients() {
		Set<String> allToNotify = new HashSet<>();
		leaderMarch().ifPresent(march -> allToNotify.add(march.getMarchEntity().getPlayerId()));
		return allToNotify;
	}

	@Override
	public void pullAttackReport() {
		List<ICYBORGWorldMarch> marchList = getParent().getParent().getPointMarches(getTerminalId());
		for (ICYBORGWorldMarch march : marchList) {
			if (march instanceof ICYBORGReportPushMarch && march != this) {
				((ICYBORGReportPushMarch) march).pushAttackReport();
			}
		}
	}

	@Override
	public void pullAttackReport(String playerId) {
		List<ICYBORGWorldMarch> marchList = getParent().getParent().getPointMarches(getTerminalId());
		for (ICYBORGWorldMarch march : marchList) {
			if (march instanceof ICYBORGReportPushMarch && march != this) {
				((ICYBORGReportPushMarch) march).pushAttackReport(playerId);
			}
		}
	}
}
