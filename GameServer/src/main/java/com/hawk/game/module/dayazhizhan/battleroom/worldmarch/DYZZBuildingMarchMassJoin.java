package com.hawk.game.module.dayazhizhan.battleroom.worldmarch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZMassJoinMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZPassiveAlarmTriggerMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZReportPushMarch;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;

public class DYZZBuildingMarchMassJoin extends IDYZZMassJoinMarch implements IDYZZReportPushMarch, IDYZZPassiveAlarmTriggerMarch {

	public DYZZBuildingMarchMassJoin(IDYZZPlayer parent) {
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
		if(getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE){
			onMarchReach(getParent());
		}

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
		List<IDYZZWorldMarch> marchList = getParent().getParent().getPointMarches(getTerminalId());
		for (IDYZZWorldMarch march : marchList) {
			if (march instanceof IDYZZReportPushMarch && march != this) {
				((IDYZZReportPushMarch) march).pushAttackReport();
			}
		}
	}

	@Override
	public void pullAttackReport(String playerId) {
		List<IDYZZWorldMarch> marchList = getParent().getParent().getPointMarches(getTerminalId());
		for (IDYZZWorldMarch march : marchList) {
			if (march instanceof IDYZZReportPushMarch && march != this) {
				((IDYZZReportPushMarch) march).pushAttackReport(playerId);
			}
		}
	}
}
