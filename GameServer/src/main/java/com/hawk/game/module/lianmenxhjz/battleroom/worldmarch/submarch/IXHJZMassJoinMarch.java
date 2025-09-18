package com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.submarch;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkTime;

import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmenxhjz.battleroom.IXHJZWorldPoint;
import com.hawk.game.module.lianmenxhjz.battleroom.entity.XHJZMarchEntity;
import com.hawk.game.module.lianmenxhjz.battleroom.player.IXHJZPlayer;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.IXHJZWorldMarch;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;

public abstract class IXHJZMassJoinMarch extends IXHJZWorldMarch {

	public IXHJZMassJoinMarch(IXHJZPlayer parent) {
		super(parent);
	}

	@Override
	public boolean isMassJoinMarch() {
		return true;
	}
	
	@Override
	public WorldMarchPB.Builder toBuilder(WorldMarchRelation relation) {
		WorldMarchPB.Builder builder = super.toBuilder(relation);
		Optional<IXHJZMassMarch> leadermOp = leaderMarch();
		if(leadermOp.isPresent()){
			builder.setMassNum(leadermOp.get().getMassJoinCnt());
			builder.setXhjzArmycnt(leadermOp.get().getMassArmyCnt());
		}
		return builder;
	}

	/**
	 * 取得队长行军
	 * 
	 * @return
	 */
	public Optional<IXHJZMassMarch> leaderMarch() {
		// 检查队长行军是否出发
		IXHJZWorldMarch massMarch = getParent().getParent().getMarch(getMarchEntity().getTargetId());
		if (massMarch instanceof IXHJZMassMarch) {
			return Optional.ofNullable((IXHJZMassMarch) massMarch);
		}
		return Optional.empty();
	}

	@Override
	public void onMarchReach(Player parent) {
		XHJZMarchEntity march = getMarchEntity();
		// 点是否已更新
		IXHJZWorldMarch massMarch = getParent().getParent().getMarch(getMarchEntity().getTargetId());
		if (massMarch == null ) {
			onMarchReturn(march.getTerminalId(), march.getOrigionId(), getArmys());
			return;
		}

		IXHJZPlayer leader = massMarch.getParent();
		// 检查队长行军是否出发
		String marchId = march.getTargetId();
		if (massMarch == null || massMarch.getMarchEntity().getStartTime() < HawkTime.getMillisecond()) {
			onMarchReturn(march.getTerminalId(), march.getOrigionId(), getArmys());
			return;
		}
		// 检查集结队伍是否超编
		Set<IXHJZWorldMarch> massMarchList = massMarch.getMassJoinMarchs(true);
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
			for (IXHJZWorldMarch joinMarch : massMarchList) {
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
			IXHJZWorldMarch back = getParent().getParent().startMarch(getParent(), leader, getParent(), WorldMarchType.XHJZ_BUILDING_SINGLE, "", 0, effParams,0);
			back.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE);
		}
		// 集结加入行军，在行军到达队长家是，需要更新点信息(如果弄不明白就全推)
		IXHJZWorldPoint targetPoint = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
		this.onMarchStop(WorldMarchStatus.MARCH_STATUS_WAITING_VALUE, stayList, targetPoint);
	}

	private void onMarchStop(int status, List<ArmyInfo> armys, IXHJZWorldPoint targetPoint) {
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

	public void detailMarchStop(IXHJZWorldPoint targetPoint) {
		WorldMarch march = getMarchEntity();
		if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {

			IXHJZWorldMarch massMarch = getParent().getParent().getMarch(march.getTargetId());
			if (massMarch != null) {
				march.setStartTime(massMarch.getMarchEntity().getStartTime() + 1); // 行军的开始时间和队长进行同步
				march.setEndTime(massMarch.getMarchEntity().getEndTime()); // 行军的结束时间和队长进行同步
				march.setMassReadyTime(massMarch.getMarchEntity().getMassReadyTime()); // 同步队长的准备时间
				march.setManorMarchReachTime(HawkApp.getInstance().getCurrentTime()); // 到达队长家的时间
			}
		}
	}

	@Override
	public void updateMarch() {
		super.updateMarch();
	}

}
