package com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.state;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import com.hawk.game.module.lianmengyqzz.battleroom.player.according.YQZZBuildStayTime;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildState;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildingHonor;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;

public class YQZZBuildingStateZhanLing extends IYQZZBuildingState {
	private IYQZZWorldMarch lastLeaderMarch;
	private long zhanlingJieshu;
	private long lastTick;

	public YQZZBuildingStateZhanLing(IYQZZBuilding build) {
		super(build);
	}

	@Override
	public void init() {
		this.zhanlingJieshu = getParent().getParent().getCurTimeMil();
		this.lastTick = zhanlingJieshu;
	}

	@Override
	public boolean onTick() {
		long timePass = getParent().getParent().getCurTimeMil() - lastTick;
		lastTick = getParent().getParent().getCurTimeMil();

		if(StringUtils.isEmpty(getParent().getOnwerGuildId())){
			IYQZZWorldMarch leaderMarch = getParent().getLeaderMarch();
			if(leaderMarch!=null){
				// 切换到争夺中
				getParent().setStateObj(new YQZZBuildingStateZhanLingZhong(getParent()));
			}
			
			return true;
		}
		
		if (getParent().isFirstControl()) {// 首控
			getParent().firstHonor();
		}

		addHonor(timePass);

		IYQZZWorldMarch leaderMarch = getParent().getLeaderMarch();
		if (lastLeaderMarch != leaderMarch) {
			getParent().worldPointUpdate();
			lastLeaderMarch = leaderMarch;
		}
		if (Objects.isNull(leaderMarch)) {
			return true;
		}

		// 有行军
		if (Objects.equals(leaderMarch.getParent().getGuildId(), getParent().getOnwerGuildId())) {
			return true;
		}

		// 切换到争夺中
		getParent().setStateObj(new YQZZBuildingStateZhanLingZhong(getParent()));
		return true;
	}

	private void addHonor(long timePass) {
		if (!getParent().getParent().getBattleStageTime().getCurStage().getStageOpenBuildList().contains(getParent().getBuildTypeId())) {
			return;
		}
		
		List<IYQZZWorldMarch> stayMarches = getParent().getParent().getPointMarches(getParent().getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);

		YQZZBuildingHonor buildingHonor = getParent().getBuildingHonor(getParent().getOnwerGuildId());
		buildingHonor.setControlTime(buildingHonor.getControlTime() + timePass);
		buildingHonor.setGuildHonor(buildingHonor.getControlTime() * 0.001 * getParent().getGuildHonorPerSecond() + 1);
		buildingHonor.setNationHonor(buildingHonor.getControlTime() * 0.001 * getParent().getNationHonorPerSecond());
		buildingHonor.setPlayerHonor(buildingHonor.getControlTime() * 0.001 * getParent().getPlayerHonorPerSecond());
		for (IYQZZWorldMarch march : stayMarches) {
			YQZZBuildStayTime stayTime = march.getParent().getBuildControlTimeStat(getParent().getBuildType());
			stayTime.setTimes(stayTime.getTimes() + timePass);
		}
	}

	@Override
	public YQZZBuildState getState() {
		return YQZZBuildState.ZHAN_LING;
	}

	@Override
	public void fillBuilder(WorldPointPB.Builder builder) {
		builder.setManorComTime(zhanlingJieshu); // 占领结束时间
	}

	@Override
	public void fillDetailBuilder(WorldPointDetailPB.Builder builder) {
		builder.setManorComTime(zhanlingJieshu); // 占领结束时间
	}
}
