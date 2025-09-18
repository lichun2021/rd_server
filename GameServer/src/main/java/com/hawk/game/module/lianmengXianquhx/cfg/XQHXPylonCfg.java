package com.hawk.game.module.lianmengXianquhx.cfg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

@HawkConfigManager.XmlResource(file = "xml/xqhx_pylon.xml")
public class XQHXPylonCfg extends HawkConfigBase {
	@Id
	private final int id;
	private final int pylonId;// ="302001"
	private final int allianceScore;// ="30"
	private final int allianceOrder;
	private final int playerScore;// ="100"

	// # 刷新时间点 开战后. /秒
	private final String refreshTime;// = 60,180,300
	// # 每次刷新数
	private final int refreshCount;// = 10
	// # 刷新点 x_y, 要求大于refreshCount. 有重合会在周边随机. 注意避开出生点, 重要建筑
	private final String refreshPoint; // 15_13,22_34,34_28,33_33,18_18,40_40,45_50,55_65,70_75,88_90,66_67,45_49
	private final int deadToWound;
	private List<Integer> refreshTimeList = new ArrayList<>();
	private List<HawkTuple2<Integer, Integer>> refreshPointList = new ArrayList<>();

	public XQHXPylonCfg() {
		id = 0;
		pylonId = 0;
		allianceScore = 0;
		allianceOrder = 0;
		playerScore = 0;
		refreshTime = "";
		refreshCount = 0;
		refreshPoint = "";
		deadToWound = 0;
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

	public int getId() {
		return id;
	}

	public int getPylonId() {
		return pylonId;
	}

	public String getRefreshTime() {
		return refreshTime;
	}

	public int getRefreshCount() {
		return refreshCount;
	}

	public int getAllianceScore() {
		return allianceScore;
	}

	public int getPlayerScore() {
		return playerScore;
	}

	public int getDeadToWound() {
		return deadToWound;
	}

	public List<HawkTuple2<Integer, Integer>> getRefreshPointList() {
		return refreshPointList;
	}

	public void setRefreshPointList(List<HawkTuple2<Integer, Integer>> refreshPointList) {
		this.refreshPointList = refreshPointList;
	}

	public int getAllianceOrder() {
		return allianceOrder;
	}

	public String getRefreshPoint() {
		return refreshPoint;
	}

}
