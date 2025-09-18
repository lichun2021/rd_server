package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

@HawkConfigManager.XmlResource(file = "xml/cyborg_monster.xml")
public class CYBORGMonsterCfg extends HawkConfigBase {
	@Id
	private final int id;
	private final int monsterId;// ="302001"
	private final int playerHonor;// ="30"
	private final int guildHonor;// ="30"
	private final int guildOrder;// ="100"
	private final int winArmyCount;// ="50000"

	// # 刷新时间点 开战后. /秒
	private final String refreshTime;// = 60,180,300
	// # 每次刷新数
	private final int refreshCount;// = 10
	// # 刷新点 x_y, 要求大于refreshCount. 有重合会在周边随机. 注意避开出生点, 重要建筑
	private final String refreshPoint; // 15_13,22_34,34_28,33_33,18_18,40_40,45_50,55_65,70_75,88_90,66_67,45_49

	private List<Integer> refreshTimeList = new ArrayList<>();
	private List<HawkTuple2<Integer, Integer>> refreshPointList = new ArrayList<>();

	public CYBORGMonsterCfg() {
		id = 0;
		monsterId = 302001;
		playerHonor = 0;
		guildHonor = 8;
		guildOrder = 500;
		winArmyCount = 150;
		refreshTime = "60,180,300";
		refreshCount = 10;
		refreshPoint = "15_13,22_34,34_28,33_33,18_18,40_40,45_50,55_65,70_75,88_90,66_67,45_49";
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

	public int getId() {
		return id;
	}

	public int getMonsterId() {
		return monsterId;
	}

	public int getPlayerHonor() {
		return playerHonor;
	}

	public int getGuildHonor() {
		return guildHonor;
	}

	public int getGuildOrder() {
		return guildOrder;
	}

	public int getWinArmyCount() {
		return winArmyCount;
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

}
