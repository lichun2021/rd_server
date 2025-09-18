package com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.state;

import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildState;

public class YQZZBuildingStateZhongLi extends IYQZZBuildingState{

	public YQZZBuildingStateZhongLi(IYQZZBuilding build) {
		super(build);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onTick() {
		IYQZZWorldMarch leaderMarch = getParent().getLeaderMarch();
		if (leaderMarch == null) {
			return true;
		}

		getParent().setStateObj(new YQZZBuildingStateZhanLingZhong(getParent()));

		return true;
	}

	@Override
	public YQZZBuildState getState() {
		// TODO Auto-generated method stub
		return YQZZBuildState.ZHONG_LI;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

}
