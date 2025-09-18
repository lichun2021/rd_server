package com.hawk.game.module.dayazhizhan.battleroom.cfg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.collect.ImmutableList;

@HawkConfigManager.KVResource(file = "xml/dyzz_energy_well.xml")
public class DYZZEnergyWellCfg extends HawkConfigBase {

	private final int redis;
	// # 刷新点 x_y, 半径为偶数 x,y相加必须为奇数
	private final String refreshPoint;// = 43_42,58_59,60_61,75_50

	// # 占领倒计时 /秒
	private final int controlCountDown;// = 30
	private final String coolDown;
	private ImmutableList<HawkTuple2<Integer, Integer>> refreshPointList;
	private HawkTuple2<Integer, Integer> coolDownArr;
	public DYZZEnergyWellCfg() {
		redis = 3;
		refreshPoint = "43_42,58_59";
		controlCountDown = 30;
		coolDown = "60_120";
	}

	@Override
	protected boolean assemble() {
		{
			List<HawkTuple2<Integer, Integer>> lsit = new ArrayList<>();
			for (String xy : refreshPoint.trim().split("\\,")) {
				String[] x_y = xy.split("_");
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]);
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.add(HawkTuples.tuple(pos[0], pos[1]));
			}
			refreshPointList = ImmutableList.copyOf(lsit);
		}

		{
			String[] x_y = coolDown.split("_");
			int[] pos = new int[2];
			pos[0] = NumberUtils.toInt(x_y[0]);
			pos[1] = NumberUtils.toInt(x_y[1]);
			coolDownArr = HawkTuples.tuple(pos[0], pos[1]);
		}
		return super.assemble();
	}

	public ImmutableList<HawkTuple2<Integer, Integer>> getRefreshPointList() {
		return refreshPointList;
	}

	public void setRefreshPointList(ImmutableList<HawkTuple2<Integer, Integer>> refreshPointList) {
		this.refreshPointList = refreshPointList;
	}

	public String getRefreshPoint() {
		return refreshPoint;
	}

	public int getControlCountDown() {
		return controlCountDown;
	}

	public int getRedis() {
		return redis;
	}

	public String getCoolDown() {
		return coolDown;
	}

	public HawkTuple2<Integer, Integer> getCoolDownArr() {
		return coolDownArr;
	}

}
