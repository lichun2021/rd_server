package com.hawk.game.module.lianmengyqzz.battleroom.worldmarch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.submarch.IYQZZPassiveAlarmTriggerMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.submarch.IYQZZReportPushMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;

/**
 * 单人铁幕装置
 */
public class YQZZBuildingMarchSingle extends IYQZZWorldMarch implements IYQZZReportPushMarch, IYQZZPassiveAlarmTriggerMarch {
	private WorldMarchType marchType;

	public YQZZBuildingMarchSingle(IYQZZPlayer parent) {
		super(parent);
	}

	@Override
	public void onMarchStart() {
		this.pushAttackReport();
	}

	@Override
	public boolean needShowInGuildWar() {
		return true;
	}

	@Override
	public WorldMarchType getMarchType() {
		return marchType;
	}

	@Override
	public Set<String> attackReportRecipients() {
		// 防守方援军
		List<IYQZZWorldMarch> helpMarchList = getParent().getParent().getPointMarches(getMarchEntity().getTerminalId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		Set<String> result = new HashSet<>();
		result.add(getMarchEntity().getTargetId());
		for (IYQZZWorldMarch march : helpMarchList) {
			result.add(march.getPlayerId());
		}
		return result;
	}

	@Override
	public void heartBeats() {
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
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
	public void onMarchReach(Player player) {
		marchReach(player);
		rePushPointReport();

	}
	
	@Override
	public void onMarchReturn() {
		// 删除行军报告
		this.removeAttackReport();
		this.pullAttackReport();
	}

	/**
	 * 行军到达只考虑去留. 目标点本身自动检测状态变化
	 */
	private void marchReach(Player player) {
		// 删除行军报告
		removeAttackReport();
		IYQZZBuilding point = (IYQZZBuilding) getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);

		point.onMarchReach(this);

	}

	/** 获取被动方联盟战争界面信息 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		IYQZZWorldPoint point = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
		IYQZZBuilding build = (IYQZZBuilding) point;

		return build.getGuildWarPassivityInfo();
	}

	@Override
	public void onMarchCallback() {
		super.onMarchCallback();
	}

	private void rePushPointReport() {
		// 删除行军报告
		removeAttackReport();
		this.pullAttackReport();
	}

	@Override
	public void pullAttackReport() {
		List<IYQZZWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (IYQZZWorldMarch march : marchList) {
			if (march instanceof IYQZZReportPushMarch && march != this) {
				((IYQZZReportPushMarch) march).pushAttackReport();
			}
		}
	}

	@Override
	public void pullAttackReport(String playerId) {
		List<IYQZZWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (IYQZZWorldMarch march : marchList) {
			if (march instanceof IYQZZReportPushMarch && march != this) {
				((IYQZZReportPushMarch) march).pushAttackReport(playerId);
			}
		}
	}

	@Override
	public void onMarchBack() {
		// TODO 加积分

		// 部队回城
		onArmyBack(getParent(), getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(), getMarchEntity().getSuperSoldierId(), this);

		this.remove();

	}

	public void setMarchType(WorldMarchType marchType) {
		this.marchType = marchType;
	}

}
