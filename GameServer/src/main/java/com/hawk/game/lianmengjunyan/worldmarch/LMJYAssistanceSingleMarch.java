package com.hawk.game.lianmengjunyan.worldmarch;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.hawk.app.HawkApp;

import com.hawk.game.lianmengjunyan.ILMJYWorldPoint;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.lianmengjunyan.worldmarch.submarch.ILMJYPassiveAlarmTriggerMarch;
import com.hawk.game.lianmengjunyan.worldmarch.submarch.ILMJYReportPushMarch;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.ArmyService;

public class LMJYAssistanceSingleMarch extends ILMJYWorldMarch implements ILMJYPassiveAlarmTriggerMarch, ILMJYReportPushMarch {

	public LMJYAssistanceSingleMarch(ILMJYPlayer parent) {
		super(parent);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.ASSISTANCE;
	}

	@Override
	public void heartBeats() {
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE) {
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
		onMarchReach(getParent());

	}

	@Override
	public void onMarchStart() {
		this.pushAttackReport();
	}

	@Override
	public void onMarchReturn() {
		removeAttackReport();
		pullAttackReport();
	}

	@Override
	public void onMarchReach(Player parent) {

		ILMJYWorldPoint point = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
		// 路点为空
		if (point == null || !(point instanceof ILMJYPlayer)) {
			onMarchReturn(getMarchEntity().getTerminalId(), getMarchEntity().getOrigionId(), getMarchEntity().getArmys());
			return;
		}
		ILMJYPlayer tarPlayer = (ILMJYPlayer) point;
		if (!Objects.equals(getParent().getGuildId(), tarPlayer.getGuildId())) {
			onMarchReturn(getMarchEntity().getTerminalId(), getMarchEntity().getOrigionId(), getMarchEntity().getArmys());
			return;
		}

		getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
		getParent().getParent().worldPointUpdate(point);
		notifyMarchEvent(MarchEvent.MARCH_DELETE);
		updateMarch();

		pullAttackReport();
	}

	@Override
	public void onMarchBack() {
		// 部队回城
		getParent().onMarchArmyBack(this);
		this.remove();

	}

	@Override
	public Set<String> attackReportRecipients() {
		Set<String> allToNotify = new HashSet<>();
		allToNotify.add(this.getMarchEntity().getTargetId());
		return allToNotify;
	}

	@Override
	public void pullAttackReport() {
		List<ILMJYWorldMarch> marchList = getParent().getParent().getWorldMarchList();
		for (ILMJYWorldMarch march : marchList) {
			if (march instanceof ILMJYReportPushMarch && march != this) {
				((ILMJYReportPushMarch) march).pushAttackReport();
			}
		}
	}

	@Override
	public void pullAttackReport(String playerId) {
		List<ILMJYWorldMarch> marchList = getParent().getParent().getPointMarches(getTerminalId());
		for (ILMJYWorldMarch march : marchList) {
			if (march instanceof ILMJYReportPushMarch && march != this) {
				((ILMJYReportPushMarch) march).pushAttackReport(playerId);
			}
		}
	}
}
