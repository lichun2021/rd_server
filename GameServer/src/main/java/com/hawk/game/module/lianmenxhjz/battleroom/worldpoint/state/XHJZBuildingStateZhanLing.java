package com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.state;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager.XHJZ_CAMP;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.IXHJZWorldMarch;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.IXHJZBuilding;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.XHJZBuildState;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.XHJZBuildingHonor;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.XHJZQuateredState;

public class XHJZBuildingStateZhanLing extends IXHJZBuildingState {
	private IXHJZWorldMarch lastLeaderMarch;
	private long zhanlingJieshu;
	private long lastTick;

	public XHJZBuildingStateZhanLing(IXHJZBuilding build) {
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

		if (getParent().getGuildCamp() == XHJZ_CAMP.NONE) {
			if (HawkTime.getMillisecond() < getParent().getProtectedEndTime()) { // 终点有保护
				return false;
			}
			IXHJZWorldMarch leaderMarch = getParent().getLeaderMarch();
			if (leaderMarch != null) {
				// 切换到争夺中
				getParent().setStateObj(new XHJZBuildingStateZhanLingZhong(getParent()));
			}

			return true;
		}

		// if (getParent().isFirstControl()) {// 首控
		// getParent().firstHonor();
		// }

		addHonor(timePass);

		IXHJZWorldMarch leaderMarch = getParent().getLeaderMarch();
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
		getParent().setStateObj(new XHJZBuildingStateZhanLingZhong(getParent()));
		return true;
	}

	private void addHonor(long timePass) {
		XHJZBuildingHonor buildingHonor = getParent().getBuildingHonor(getParent().getOnwerGuildId());
		buildingHonor.setControlTime(buildingHonor.getControlTime() + timePass);
		buildingHonor.setGuildHonor(buildingHonor.getControlTime() * 0.001 * getParent().getAllianceScoreAdd());
	}

	@Override
	public XHJZBuildState getState() {
		return XHJZBuildState.ZHAN_LING;
	}

	@Override
	public void fillBuilder(WorldPointPB.Builder builder) {
		builder.setManorComTime(zhanlingJieshu); // 占领结束时间
	}

	@Override
	public void fillDetailBuilder(WorldPointDetailPB.Builder builder) {
		builder.setManorComTime(zhanlingJieshu); // 占领结束时间
	}

	@Override
	public XHJZQuateredState getMarchQuateredStatus(IXHJZWorldMarch march) {
		XHJZQuateredState.Builder state = XHJZQuateredState.newBuilder();
		state.setState(getState().intValue());
		// state.setZhanLingJieShu(zhanlingJieshu);
		// state.setZhuShouKaishi(zhanlingStart);
		return state.build();
	}

}
