package com.hawk.game.module.lianmengXianquhx.worldmarch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.app.HawkApp;

import com.hawk.game.module.lianmengXianquhx.IXQHXWorldPoint;
import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;
import com.hawk.game.module.lianmengXianquhx.worldmarch.submarch.IXQHXMassMarch;
import com.hawk.game.module.lianmengXianquhx.worldmarch.submarch.IXQHXPassiveAlarmTriggerMarch;
import com.hawk.game.module.lianmengXianquhx.worldmarch.submarch.IXQHXReportPushMarch;
import com.hawk.game.module.lianmengXianquhx.worldpoint.IXQHXBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;

/** 集结
 */
public class XQHXBuildingMarchMass extends IXQHXMassMarch implements IXQHXReportPushMarch , IXQHXPassiveAlarmTriggerMarch {

	private WorldMarchType marchType;
	private WorldMarchType joinMassType;

	public XQHXBuildingMarchMass(IXQHXPlayer parent) {
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
		IXQHXBuilding point = (IXQHXBuilding) getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
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
		IXQHXWorldPoint point = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
		IXQHXBuilding build = (IXQHXBuilding) point;

		return build.getGuildWarPassivityInfo();
	}

	@Override
	public boolean isMassMarch() {
		return true;
	}

	@Override
	public boolean isEvident() {
		return IXQHXReportPushMarch.super.isEvident() || this.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
	}

	@Override
	public Set<String> attackReportRecipients() {
		// 防守方援军
		List<IXQHXWorldMarch> helpMarchList = getParent().getParent().getPointMarches(getMarchEntity().getTerminalId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		Set<String> result = new HashSet<>();
		for (IXQHXWorldMarch march : helpMarchList) {
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
		List<IXQHXWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (IXQHXWorldMarch march : marchList) {
			if (march instanceof IXQHXReportPushMarch && march != this) {
				((IXQHXReportPushMarch) march).pushAttackReport();
			}
		}
	}

	@Override
	public void pullAttackReport(String playerId) {
		List<IXQHXWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (IXQHXWorldMarch march : marchList) {
			if (march instanceof IXQHXReportPushMarch && march != this) {
				((IXQHXReportPushMarch) march).pushAttackReport(playerId);
			}
		}
	}

}
