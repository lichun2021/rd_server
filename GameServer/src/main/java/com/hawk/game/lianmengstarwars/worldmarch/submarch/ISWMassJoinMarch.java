package com.hawk.game.lianmengstarwars.worldmarch.submarch;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkTime;

import com.hawk.game.lianmengstarwars.ISWWorldPoint;
import com.hawk.game.lianmengstarwars.entity.SWMarchEntity;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.lianmengstarwars.worldmarch.ISWWorldMarch;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;

public abstract class ISWMassJoinMarch extends ISWWorldMarch {

	public ISWMassJoinMarch(ISWPlayer parent) {
		super(parent);
	}

	@Override
	public boolean isMassJoinMarch() {
		return true;
	}

	/**
	 * 取得队长行军
	 * 
	 * @return
	 */
	public Optional<ISWMassMarch> leaderMarch() {
		// 检查队长行军是否出发
		ISWWorldMarch massMarch = getParent().getParent().getMarch(getMarchEntity().getTargetId());
		if (massMarch instanceof ISWMassMarch) {
			return Optional.ofNullable((ISWMassMarch) massMarch);
		}
		return Optional.empty();
	}

	@Override
	public void onMarchReach(Player parent) {
		SWMarchEntity march = getMarchEntity();
		// 点是否已更新
		ISWWorldPoint targetPoint = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
		if (targetPoint == null || !(targetPoint instanceof ISWPlayer)) {
			onMarchReturn(march.getTerminalId(), march.getOrigionId(), getArmys());
			return;
		}

		ISWPlayer leader = (ISWPlayer) targetPoint;
		// 检查队长行军是否出发
		String marchId = march.getTargetId();
		ISWMassMarch massMarch = (ISWMassMarch) getParent().getParent().getPlayerMarch(leader.getId(), marchId);
		if (massMarch == null || massMarch.getMarchEntity().getStartTime() < HawkTime.getMillisecond()) {
			onMarchReturn(march.getTerminalId(), march.getOrigionId(), getArmys());
			return;
		}
		// 检查集结队伍是否超编
		Set<ISWWorldMarch> massMarchList = massMarch.getMassJoinMarchs(true);
		int count = massMarchList != null ? massMarchList.size() : 0;
		if (count >= leader.getMaxMassJoinMarchNum() + massMarch.getMarchEntity().getBuyItemTimes()) {
			onMarchReturn(march.getTerminalId(), march.getOrigionId(), getArmys());
			return;
		}
		// 检查集结士兵数是否超上限
		int maxMassSoldierNum = massMarch.getMaxMassJoinSoldierNum(leader, getParent());
		// 队长现在带的兵力人口
		int curPopulationCnt = WorldUtil.calcSoldierCnt(massMarch.getMarchEntity().getArmys());
		if (massMarchList != null && massMarchList.size() > 0) {
			for (ISWWorldMarch joinMarch : massMarchList) {
				if (joinMarch.getPlayerId().equals(march.getPlayerId())) {
					continue;
				}
				curPopulationCnt += WorldUtil.calcSoldierCnt(joinMarch.getMarchEntity().getArmys());
			}
		}
		// 士兵人口数已达上限
		if (curPopulationCnt >= maxMassSoldierNum) {
			onMarchReturn(march.getTerminalId(), march.getOrigionId(), getArmys());
			return;
		}
		// 计算剩余兵力空位
		int remainSpace = maxMassSoldierNum - curPopulationCnt;
		List<ArmyInfo> stayList = new ArrayList<ArmyInfo>();
		List<ArmyInfo> backList = new ArrayList<ArmyInfo>();
		WorldUtil.calcStayArmy(march, remainSpace, stayList, backList);

		// 回家的士兵生成新行军
		if (backList.size() > 0) {
			EffectParams effParams = new EffectParams();
			effParams.setArmys(backList);
			ISWWorldMarch back = getParent().getParent().startMarch(getParent(), leader, getParent(), WorldMarchType.ASSISTANCE, "", 0, effParams);
			back.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE);
		}
		// 集结加入行军，在行军到达队长家是，需要更新点信息(如果弄不明白就全推)

		this.onMarchStop(WorldMarchStatus.MARCH_STATUS_WAITING_VALUE, stayList, targetPoint);
	}

	private void onMarchStop(int status, List<ArmyInfo> armys, ISWWorldPoint targetPoint) {
		if (this.getMarchEntity().isInvalid()) {
			return;
		}
		WorldMarch march = getMarchEntity();
		WorldMarchService.logger.info("world march stop, marchData: {}", march);
		// 通用参数设置
		march.setMarchStatus(status);
		if (armys != null) {
			march.setArmys(armys);
		}
		// 不同类型自己的特殊处理
		detailMarchStop(targetPoint);

		// 更新
		this.updateMarch();

	}

	public void detailMarchStop(ISWWorldPoint targetPoint) {
		WorldMarch march = getMarchEntity();
		if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {

			ISWWorldMarch massMarch = getParent().getParent().getMarch(march.getTargetId());
			if (massMarch != null) {
				march.setStartTime(massMarch.getMarchEntity().getStartTime() + 1); // 行军的开始时间和队长进行同步
				march.setEndTime(massMarch.getMarchEntity().getEndTime()); // 行军的结束时间和队长进行同步
				march.setMassReadyTime(massMarch.getMarchEntity().getMassReadyTime()); // 同步队长的准备时间
				march.setManorMarchReachTime(getParent().getParent().getCurTimeMil()); // 到达队长家的时间
			}
		}
	}

	@Override
	public void updateMarch() {
		super.updateMarch();
	}

}
