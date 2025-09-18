package com.hawk.game.module.dayazhizhan.battleroom.cfg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.collect.ImmutableList;

@HawkConfigManager.KVResource(file = "xml/dyzz_fuelbank_small.xml")
public class DYZZFuelBankSmallCfg extends HawkConfigBase {
	// # 建筑半径
	private final int redis;// = 3
	private final int resourceId;
	// # 采集速度 /秒
	private final String collectSpeed; // = 8
	// # 初始值
	private final int totalRes;// = 500
	// # 最小值. 当小于该数值, 行军撤离时将主动删除资源点
	private final int minRes;// = 150
	// # 刷新点 x_y, 注意避开出生点, 重要建筑
	private final String refreshPoint; // 15_13,22_34,34_28,33_33,18_18,40_40,45_50,55_65,70_75,88_90,66_67,45_49
	// # 最小存在数 少于会补充到
	private final int minNum;// = 2
	private ImmutableList<HawkTuple2<Integer, Integer>> refreshPointList;
	private ImmutableList<Double> collectSpeedList;

	public DYZZFuelBankSmallCfg() {
		resourceId = 300502;
		collectSpeed = "2_4_6_8_10_12";
		totalRes = 500;
		minRes = 150;
		refreshPoint = "15_13,22_34,34_28,33_33,18_18,40_40,45_50,55_65,70_75,88_90,66_67,45_49";
		minNum = 2;
		redis = 1;
	}

	@Override
	protected boolean assemble() {
		List<HawkTuple2<Integer, Integer>> rlist = new ArrayList<>();
		for (String xy : refreshPoint.trim().split("\\,")) {
			String[] x_y = xy.split("_");
			int[] pos = new int[2];
			pos[0] = NumberUtils.toInt(x_y[0]);
			pos[1] = NumberUtils.toInt(x_y[1]);
			rlist.add(HawkTuples.tuple(pos[0], pos[1]));
		}
		refreshPointList = ImmutableList.copyOf(rlist);

		List<Double> clist = new ArrayList<>();
		for (String d : collectSpeed.trim().split("_")) {
			clist.add(Double.valueOf(d));
		}
		collectSpeedList = ImmutableList.copyOf(clist);
		return super.assemble();
	}

	public int[] randomBoinPoint() {
		int index = HawkRand.randInt(0, 100) % refreshPointList.size();
		HawkTuple2<Integer, Integer> p = refreshPointList.get(index);
		return new int[] { p.first, p.second };
	}

	public List<HawkTuple2<Integer, Integer>> getRefreshPointList() {
		return refreshPointList;
	}

	public String getCollectSpeed() {
		return collectSpeed;
	}

	public int getTotalRes() {
		return totalRes;
	}

	public int getMinRes() {
		return minRes;
	}

	public int getResourceId() {
		return resourceId;
	}

	public String getRefreshPoint() {
		return refreshPoint;
	}

	public int getMinNum() {
		return minNum;
	}

	public int getRedis() {
		return redis;
	}

	public ImmutableList<Double> getCollectSpeedList() {
		return collectSpeedList;
	}

}
