package com.hawk.game.module.lianmengyqzz.battleroom.cfg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;

import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildType;

@HawkConfigManager.XmlResource(file = "xml/moon_war_resource.xml")
public class YQZZResourceCfg extends HawkConfigBase {
	@Id
	private final int id;
	private final int resourceId;// ="302001"
	private final int nationScore;// ="30"
	private final int allianceScore;// ="30"
	private final int playerScore;// ="100"

	// # 刷新时间点 开战后. /秒
	private final String refreshTime;// = 60,180,300
	// # 每次刷新数
	private final int refreshCount;// = 10
	// # 刷新点 x_y, 要求大于refreshCount. 有重合会在周边随机. 注意避开出生点, 重要建筑
	private final String refreshBuildType; // 1,2,3
	private final int awardCount;//="100"  // 每采集xxx 随机一次奖励 
	private final int awardId;//="2500205"
	
	private List<Integer> refreshTimeList = new ArrayList<>();
	private List<YQZZBuildType> refreshPointList = new ArrayList<>();

	public YQZZResourceCfg() {
		id = 0;
		resourceId = 0;
		nationScore = 0;
		allianceScore = 0;
		playerScore = 0;
		refreshTime = "";
		refreshCount = 0;
		refreshBuildType = "";
		awardCount = 0;
		awardId = 0;
	}

	@Override
	protected boolean assemble() {
		for (String xy : refreshBuildType.trim().split("\\,")) {
			refreshPointList.add(YQZZBuildType.valueOf(NumberUtils.toInt(xy)));
		}
		for (String xy : refreshTime.trim().split("\\,")) {
			refreshTimeList.add(NumberUtils.toInt(xy));
		}

		return super.assemble();
	}

	public YQZZBuildType randomBoinPoint() {
		int index = HawkRand.randInt(0, 100) % refreshPointList.size();
		return refreshPointList.get(index);
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

	public int getResourceId() {
		return resourceId;
	}

	public String getRefreshTime() {
		return refreshTime;
	}

	public int getRefreshCount() {
		return refreshCount;
	}

	public int getNationScore() {
		return nationScore;
	}

	public int getAllianceScore() {
		return allianceScore;
	}

	public int getPlayerScore() {
		return playerScore;
	}

	public String getRefreshBuildType() {
		return refreshBuildType;
	}

	public List<YQZZBuildType> getRefreshPointList() {
		return refreshPointList;
	}

	public void setRefreshPointList(List<YQZZBuildType> refreshPointList) {
		this.refreshPointList = refreshPointList;
	}

	public int getAwardCount() {
		return awardCount;
	}

	public int getAwardId() {
		return awardId;
	}

}
