package com.hawk.game.module.lianmengfgyl.battleroom.worldmarch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.app.HawkApp;

import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.submarch.IFGYLMassJoinMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.submarch.IFGYLPassiveAlarmTriggerMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.submarch.IFGYLReportPushMarch;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;

public class FGYLBuildingMarchMassJoin extends IFGYLMassJoinMarch implements IFGYLReportPushMarch, IFGYLPassiveAlarmTriggerMarch {

	public FGYLBuildingMarchMassJoin(IFGYLPlayer parent) {
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
		long currTime = HawkApp.getInstance().getCurrentTime();
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
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
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
		updateMarch();

		this.removeAttackReport();
	}

	/*** 返回家中 */
	public void onMarchBack() {
		// 部队回城
		onArmyBack();

		this.remove();
		if (getUseHonor() > 0) {
			IFGYLPlayer player = getParent();
			player.setSkillOrder(player.getSkillOrder() + getUseHonor());
			player.getPush().syncFGYLPlayerInfo();
		}
	}

	@Override
	public Set<String> attackReportRecipients() {
		Set<String> allToNotify = new HashSet<>();
		leaderMarch().ifPresent(march -> allToNotify.add(march.getMarchEntity().getPlayerId()));
		return allToNotify;
	}

	@Override
	public void pullAttackReport() {
		List<IFGYLWorldMarch> marchList = getParent().getParent().getPointMarches(getTerminalId());
		for (IFGYLWorldMarch march : marchList) {
			if (march instanceof IFGYLReportPushMarch && march != this) {
				((IFGYLReportPushMarch) march).pushAttackReport();
			}
		}
	}

	@Override
	public void pullAttackReport(String playerId) {
		List<IFGYLWorldMarch> marchList = getParent().getParent().getPointMarches(getTerminalId());
		for (IFGYLWorldMarch march : marchList) {
			if (march instanceof IFGYLReportPushMarch && march != this) {
				((IFGYLReportPushMarch) march).pushAttackReport(playerId);
			}
		}
	}
}
