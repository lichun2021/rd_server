package com.hawk.game.lianmengcyb.worldmarch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hawk.game.lianmengcyb.ICYBORGWorldPoint;
import com.hawk.game.lianmengcyb.order.CYBORGOrder;
import com.hawk.game.lianmengcyb.order.CYBORGOrder1001;
import com.hawk.game.lianmengcyb.order.CYBORGOrderCollection;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldmarch.submarch.ICYBORGPassiveAlarmTriggerMarch;
import com.hawk.game.lianmengcyb.worldmarch.submarch.ICYBORGReportPushMarch;
import com.hawk.game.lianmengcyb.worldpoint.ICYBORGBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;

/**
 * 单人铁幕装置
 */
public class CYBORGBuildingMarchSingle extends ICYBORGWorldMarch implements ICYBORGReportPushMarch, ICYBORGPassiveAlarmTriggerMarch {
	private WorldMarchType marchType;

	public CYBORGBuildingMarchSingle(ICYBORGPlayer parent) {
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
	public long getMarchNeedTime() {
		CYBORGOrderCollection orderCollection = getParent().getParent().getCampBase(getParent().getCamp()).orderCollection;
		for (CYBORGOrder order : orderCollection.getOrders()) {
			if (order.inEffect() && order instanceof CYBORGOrder1001) {
				CYBORGOrder1001 speedSkill = (CYBORGOrder1001) order;
				if (speedSkill.getTarX() == getMarchEntity().getTerminalX() && speedSkill.getTarY() == getMarchEntity().getTerminalY()) {
					return speedSkill.getConfig().getP1() * 1000;
				}
			}
		}
		
		return super.getMarchNeedTime();
	}

	@Override
	public Set<String> attackReportRecipients() {
		// 防守方援军
		List<ICYBORGWorldMarch> helpMarchList = getParent().getParent().getPointMarches(getMarchEntity().getTerminalId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		Set<String> result = new HashSet<>();
		result.add(getMarchEntity().getTargetId());
		for (ICYBORGWorldMarch march : helpMarchList) {
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
		ICYBORGBuilding point = (ICYBORGBuilding) getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);

		point.onMarchReach(this);

	}

	/** 获取被动方联盟战争界面信息 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		ICYBORGWorldPoint point = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
		ICYBORGBuilding build = (ICYBORGBuilding) point;

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
