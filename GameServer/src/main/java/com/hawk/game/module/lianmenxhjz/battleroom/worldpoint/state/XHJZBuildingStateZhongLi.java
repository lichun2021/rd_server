package com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.state;

import org.hawk.os.HawkTime;

import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.IXHJZWorldMarch;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.IXHJZBuilding;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.XHJZBuildState;

public class XHJZBuildingStateZhongLi extends IXHJZBuildingState{

	public XHJZBuildingStateZhongLi(IXHJZBuilding build) {
		super(build);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onTick() {
		if (HawkTime.getMillisecond() < getParent().getProtectedEndTime()) { // 终点有保护
			return false;
		}
		IXHJZWorldMarch leaderMarch = getParent().getLeaderMarch();
		if (leaderMarch == null) {
			return true;
		}

		getParent().setStateObj(new XHJZBuildingStateZhanLingZhong(getParent()));

		return true;
	}

	@Override
	public XHJZBuildState getState() {
		// TODO Auto-generated method stub
		return XHJZBuildState.ZHONG_LI;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

}
