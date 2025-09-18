package com.hawk.game.module.dayazhizhan.battleroom.worldmarch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.dayazhizhan.battleroom.IDYZZWorldPoint;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZPassiveAlarmTriggerMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZReportPushMarch;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.march.IWorldMarch;

public class DYZZAssistanceSingleMarch extends IDYZZWorldMarch implements IDYZZPassiveAlarmTriggerMarch, IDYZZReportPushMarch {

	public DYZZAssistanceSingleMarch(IDYZZPlayer parent) {
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
		pullAttackReport();
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

		IDYZZWorldPoint point = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
		// 路点为空
		if (point == null || !(point instanceof IDYZZPlayer)) {
			onMarchReturn(getMarchEntity().getTerminalId(), getMarchEntity().getOrigionId(), getMarchEntity().getArmys());
			return;
		}
		IDYZZPlayer tarPlayer = (IDYZZPlayer) point;
		if (!Objects.equals(getParent().getDYZZGuildId(), tarPlayer.getDYZZGuildId())) {
			onMarchReturn(getMarchEntity().getTerminalId(), getMarchEntity().getOrigionId(), getMarchEntity().getArmys());
			return;
		}

		// 检查集结士兵数是否超上限
		int maxPopulationCnt = tarPlayer.getMaxAssistSoldier();
		int curPopulationCnt = 0;// 已援助士兵数目
		List<IDYZZWorldMarch> helpMarchList = getParent().getParent().getPointMarches(getMarchEntity().getTerminalId(),
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST,
				WorldMarchType.ASSISTANCE);

		// 计算已援助士兵数量
		if (helpMarchList != null && helpMarchList.size() > 0) {
			for (IWorldMarch helpMarch : helpMarchList) {
				curPopulationCnt += WorldUtil.calcSoldierCnt(helpMarch.getMarchEntity().getArmys());
			}
		}
		// 士兵数已达上限
		if (curPopulationCnt >= maxPopulationCnt) {
			onMarchReturn(getMarchEntity().getTerminalId(), getMarchEntity().getOrigionId(), getMarchEntity().getArmys());
			return;
		}
		// 计算剩余兵力空位
		int remainSpace = maxPopulationCnt - curPopulationCnt;

		List<ArmyInfo> stayList = new ArrayList<ArmyInfo>();
		List<ArmyInfo> backList = new ArrayList<ArmyInfo>();
		WorldUtil.calcStayArmy(getMarchEntity(), remainSpace, stayList, backList);

		// 回家的士兵生成新行军
		if (backList.size() > 0) {
			EffectParams effParams = new EffectParams();
			effParams.setArmys(backList);
			IDYZZWorldMarch back = getParent().getParent().startMarch(getParent(), point, getParent(), WorldMarchType.ASSISTANCE, "",0, effParams);
			back.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE);
		}
		getMarchEntity().setArmys(stayList);
		getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
		getParent().getParent().worldPointUpdate(point);
		notifyMarchEvent(MarchEvent.MARCH_DELETE);
		updateMarch();

	}

	@Override
	public void onMarchBack() {
		// 部队回城
		onArmyBack(getParent(), getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(), getMarchEntity().getSuperSoldierId(), this);

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
