package com.hawk.game.module.lianmengtaiboliya.cfg;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

@HawkConfigManager.XmlResource(file = "xml/tbly_fuelbank.xml")
public class TBLYFuelBankCfg extends HawkConfigBase {
	@Id
	private final int id;
	private final int resourceId;
	private final int level;
	// # 采集速度 /秒
	private final double collectSpeed; // = 8
	// # 初始值
	private final int totalRes;// = 500
	// # 最小值. 当小于该数值, 行军撤离时将主动删除资源点
	private final int minRes;// = 150
	// # 刷新时间点 开战后. /秒
	private final String refreshTime;// = 60,180,300
	// # 每次刷新数
	private final int refreshCount;// = 10
	// # 刷新点 x_y, 要求大于refreshCount. 有重合会在周边随机. 注意避开出生点, 重要建筑
	private final String refreshPoint; // 15_13,22_34,34_28,33_33,18_18,40_40,45_50,55_65,70_75,88_90,66_67,45_49
	private final int collectArmyMin;// = 50000
	// # 联盟积分转化率 才集1资源对应联盟积分up
	private final double guildHonorRate;// = 0.2
	private List<Integer> refreshTimeList = new ArrayList<>();
	private List<HawkTuple2<Integer, Integer>> refreshPointList = new ArrayList<>();

	public TBLYFuelBankCfg() {
		id = 0;
		resourceId = 300502;
		level = 0;
		collectSpeed = 8;
		totalRes = 500;
		minRes = 150;
		refreshTime = "60,180,300";
		refreshCount = 10;
		refreshPoint = "15_13,22_34,34_28,33_33,18_18,40_40,45_50,55_65,70_75,88_90,66_67,45_49";
		collectArmyMin = 50000;
		guildHonorRate = 0.2;
	}

	@Override
	protected boolean assemble() {
		for (String xy : refreshPoint.trim().split("\\,")) {
			String[] x_y = xy.split("_");
			int[] pos = new int[2];
			pos[0] = NumberUtils.toInt(x_y[0]);
			pos[1] = NumberUtils.toInt(x_y[1]);
			refreshPointList.add(HawkTuples.tuple(pos[0], pos[1]));
		}
		for (String xy : refreshTime.trim().split("\\,")) {
			refreshTimeList.add(NumberUtils.toInt(xy));
		}

		return super.assemble();
	}
	
	public int[] randomBoinPoint() {
		int index = HawkRand.randInt(0, 100) % refreshPointList.size();
		HawkTuple2<Integer, Integer> p = refreshPointList.get(index);
		return new int[] { p.first, p.second };
	}

	public List<Integer> getRefreshTimeList() {
		return refreshTimeList;
	}

	public void setRefreshTimeList(List<Integer> refreshTimeList) {
		this.refreshTimeList = refreshTimeList;
	}

	public List<HawkTuple2<Integer, Integer>> getRefreshPointList() {
		return refreshPointList;
	}

	public void setRefreshPointList(List<HawkTuple2<Integer, Integer>> refreshPointList) {
		this.refreshPointList = refreshPointList;
	}

	public double getCollectSpeed() {
		return collectSpeed;
	}

	public int getTotalRes() {
		return totalRes;
	}

	public int getMinRes() {
		return minRes;
	}

	public String getRefreshTime() {
		return refreshTime;
	}

	public int getRefreshCount() {
		return refreshCount;
	}

	public String getRefreshPoint() {
		return refreshPoint;
	}

	public int getCollectArmyMin() {
		return collectArmyMin;
	}

	public double getGuildHonorRate() {
		return guildHonorRate;
	}

	public int getId() {
		return id;
	}

	public int getResourceId() {
		return resourceId;
	}

	public int getLevel() {
		return level;
	}

}
