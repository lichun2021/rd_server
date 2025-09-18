package com.hawk.game.module.lianmenxhjz.battleroom.worldmarch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkTime;

import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.module.lianmenxhjz.battleroom.IXHJZWorldPoint;
import com.hawk.game.module.lianmenxhjz.battleroom.entity.XHJZMarchEntity;
import com.hawk.game.module.lianmenxhjz.battleroom.player.IXHJZPlayer;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.submarch.IXHJZMassMarch;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.submarch.IXHJZPassiveAlarmTriggerMarch;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.submarch.IXHJZReportPushMarch;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.IXHJZBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.Position;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.util.GameUtil;

/** 集结
 */
public class XHJZBuildingMarchMass extends IXHJZMassMarch implements IXHJZReportPushMarch , IXHJZPassiveAlarmTriggerMarch {

	private WorldMarchType marchType;
	private WorldMarchType joinMassType;

	public XHJZBuildingMarchMass(IXHJZPlayer parent) {
		super(parent);
	}

	@Override
	public void heartBeats() {
		
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			tryNextPoint();
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
	
	private void tryNextPoint() {
		if (getNextPoints().isEmpty()) {
			return;
		}
		Position next = getNextPoints().remove(0);

		// 修改状态
		XHJZMarchEntity marchEntity = getMarchEntity();
		marchEntity.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_VALUE);

		// 状态改变时修改加速召回信息
		marchEntity.setItemUseTime(0);
		marchEntity.setItemUseX(0.0d);
		marchEntity.setItemUseY(0.0d);
		marchEntity.setSpeedUpTimes(0);

		marchEntity.setOrigionId(marchEntity.getTerminalId());
		marchEntity.setTerminalId(GameUtil.combineXAndY(next.getX(), next.getY()));

		long millisecond = HawkTime.getMillisecond();
		marchEntity.setStartTime(millisecond);
		marchEntity.setEndTime(next.getReachTime());
		marchEntity.setMarchJourneyTime(next.getReachTime()- millisecond);

		// 行军上需要显示的作用号
		List<Integer> marchShowEffList = new ArrayList<>();
		int[] marchShowEffs = WorldMarchConstProperty.getInstance().getMarchShowEffArray();
		if (marchShowEffs != null) {
			for (int i = 0; i < marchShowEffs.length; i++) {
				int effVal = getParent().getEffect().getEffVal(EffType.valueOf(marchShowEffs[i]));
				if (effVal > 0) {
					marchShowEffList.add(effVal);
				}
			}
		}

		if (!marchShowEffList.isEmpty()) {
			this.getMarchEntity().resetEffect(marchShowEffList);
		}
		int gasoline = next.getXhjzGasoline();
		setGasoline(getGasoline() - gasoline);
		
		
		// 刷新出征
		this.updateMarch();

		Set<IXHJZWorldMarch> joinMarchs = getMassJoinMarchs(false);
		for (IXHJZWorldMarch joinMarch : joinMarchs) {
			joinMarch.getMarchEntity().setTerminalId(getMarchEntity().getTerminalId());
			// 此处要将加入的行军状态改成集结行军状态
			joinMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_JOIN_MARCH_VALUE);
			joinMarch.getMarchEntity().setMarchJourneyTime(getMarchEntity().getMarchJourneyTime());
			joinMarch.getMarchEntity().setStartTime(getMarchEntity().getStartTime());
			joinMarch.getMarchEntity().setEndTime(getMarchEntity().getEndTime());
			joinMarch.setGasoline(joinMarch.getGasoline() - gasoline);
			joinMarch.updateMarch();
		}


		IXHJZBuilding point = (IXHJZBuilding) getParent().getParent().getWorldPoint(getMarchEntity().getOrigionX(), getMarchEntity().getOrigionY()).orElse(null);
		point.worldPointUpdate();
	}

	@Override
	public void onMarchBack() {
		// 部队回城
		onArmyBack();

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
		IXHJZBuilding point = (IXHJZBuilding) getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
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
		Position ps = getTerminalPosition();
		IXHJZWorldPoint point = getParent().getParent().getWorldPoint(ps.getX(), ps.getY()).orElse(null);
		IXHJZBuilding build = (IXHJZBuilding) point;

		return build.getGuildWarPassivityInfo();
	}

	@Override
	public boolean isMassMarch() {
		return true;
	}

	@Override
	public boolean isEvident() {
		return IXHJZReportPushMarch.super.isEvident() || this.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
	}

	@Override
	public Set<String> attackReportRecipients() {
		// 防守方援军
		List<IXHJZWorldMarch> helpMarchList = getParent().getParent().getPointMarches(getMarchEntity().getTerminalId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		Set<String> result = new HashSet<>();
		for (IXHJZWorldMarch march : helpMarchList) {
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
		List<IXHJZWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (IXHJZWorldMarch march : marchList) {
			if (march instanceof IXHJZReportPushMarch && march != this) {
				((IXHJZReportPushMarch) march).pushAttackReport();
			}
		}
	}

	@Override
	public void pullAttackReport(String playerId) {
		List<IXHJZWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (IXHJZWorldMarch march : marchList) {
			if (march instanceof IXHJZReportPushMarch && march != this) {
				((IXHJZReportPushMarch) march).pushAttackReport(playerId);
			}
		}
	}

}
