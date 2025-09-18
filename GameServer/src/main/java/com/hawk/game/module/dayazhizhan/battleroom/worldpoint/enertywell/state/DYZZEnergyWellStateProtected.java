package com.hawk.game.module.dayazhizhan.battleroom.worldpoint.enertywell.state;

import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.enertywell.DYZZEnergyWell;
import com.hawk.game.protocol.DYZZ.DYZZBuildState;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;

public class DYZZEnergyWellStateProtected extends IDYZZEnergyWellState {
	private long endtime;

	public DYZZEnergyWellStateProtected(DYZZEnergyWell parent) {
		super(parent);
	}

	@SuppressWarnings("static-access")
	@Override
	public void init() {
		HawkTuple2<Integer, Integer> arr = getParent().getCfg().getCoolDownArr();
		if (getParent().getParent().getCollectStartTime() > getParent().getParent().getCurTimeMil()) {
			endtime = getParent().getParent().getCollectStartTime() + HawkRand.randInt(arr.first, arr.second) * 1000;
		} else {
			endtime = getParent().getParent().getCurTimeMil() + HawkRand.randInt(arr.first, arr.second) * 1000;
		}
	}

	@Override
	public boolean onTick() {
		if (getParent().getParent().getCurTimeMil() > endtime) {
			getParent().setStateObj(new DYZZEnergyWellStateZhongLi(getParent()));
		}
		return true;
	}

	@Override
	public DYZZBuildState getState() {
		return DYZZBuildState.PROTECTED;
	}

	@Override
	public long getProtectedEndTime() {
		return endtime;
	}

	@Override
	public void fillBuilder(WorldPointPB.Builder builder) {
		builder.setProtectedEndTime(endtime);
	}

	@Override
	public void fillDetailBuilder(WorldPointDetailPB.Builder builder) {
		builder.setProtectedEndTime(endtime);
	}

}
