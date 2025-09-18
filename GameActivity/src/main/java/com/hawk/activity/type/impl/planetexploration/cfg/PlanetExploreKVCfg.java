package com.hawk.activity.type.impl.planetexploration.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.KVResource(file = "activity/planet_exploration/planet_exploration_cfg.xml")
public class PlanetExploreKVCfg extends HawkConfigBase {
	
	private final long serverDelay; //服务器开服延时开启活动时间；单位：秒
	
	private final String useItem; //抽奖道具
	
	private final String pointRandom; //每日结算刷矿时间点
	
	private final String refreshTime; //每日结算刷矿时间点
	
	private final long serverMaxPoint; //服务器积分上限
	
	private final int refreshPoint;  //每多少积分刷出一个矿
	
	private final int personRankSize; //个人排名记录的总名次
	
	private final int onceMax; //单次操作，抽奖最大次数
	
	private final int noExplorationTime; //活动结束前X秒不能探索
	
	private final String price; //购买道具消耗
	
	private final int refreshTargetId; //刷新的锅的ID，配置：res_treasure表的ID
	
	private final String gatherOnce; //拉锅1次获得的奖励数  gatherOnce = 30000_802005_10
	
	private final int serverLogMax; //挖掘记录->世界刷新记录显示条目上限
	private final int playerLogMax; //挖掘记录->个人采集记录显示条目上限
	
	private final int drawMax; //活动期间抽奖次数上限
	private final int playerDailyGetMax; //单人采星能矿每日上限
	
	private RewardItem.Builder exploreItem = null;
	private int[] pointLowHigh = new int[2];
	private String[] refreshTimeArr;
	
	public PlanetExploreKVCfg(){
		this.serverDelay = 0;
		this.useItem = "";
		this.pointRandom = "";
		this.refreshTime = "";
		this.serverMaxPoint = 0;
		this.refreshPoint = 0;
		this.personRankSize = 100;
		this.onceMax = 10;
		this.noExplorationTime = 0;
		this.price = "";
		this.refreshTargetId = 0;
		this.gatherOnce = "";
		this.serverLogMax = 0;
		this.playerLogMax = 0;
		this.drawMax = 0;
		this.playerDailyGetMax = 0;
	}
	
	public boolean assemble() {
		exploreItem = RewardHelper.toRewardItem(useItem);
		String[] lowHight = pointRandom.split("_");
		pointLowHigh[0] = Integer.parseInt(lowHight[0]);
		pointLowHigh[1] = Integer.parseInt(lowHight[1]);
		refreshTimeArr = refreshTime.split("_");
		long[] timeArr = new long[refreshTimeArr.length];
		int index = 0;
		String timeNowStr = HawkTime.formatNowTime();
		String dayStr = timeNowStr.split(" ")[0];
		for (String timeStr : refreshTimeArr) {
			String time = String.format("%s %s", dayStr, timeStr);
			timeArr[index++] = HawkTime.parseTime(time);
		}
		
		index = 0;
		while (index < timeArr.length - 1) {
			if (timeArr[index] >= timeArr[index+1]) {
				return false;
			}
			index++;
		}
		
		return super.assemble();
	}
	
	public long[] getRefreshTimeArr() {
		long[] timeArr = new long[refreshTimeArr.length];
		int index = 0;
		String timeNowStr = HawkTime.formatNowTime();
		String dayStr = timeNowStr.split(" ")[0];
		for (String timeStr : refreshTimeArr) {
			String time = String.format("%s %s", dayStr, timeStr);
			timeArr[index++] = HawkTime.parseTime(time);
		}
		return timeArr;
	}
	
	public int getItemId() {
		return exploreItem.getItemId();
	}
	
	public int getConsumeCount() {
		return (int)exploreItem.getItemCount();
	}
	
	public int[] getPointLowHigh() {
		return pointLowHigh;
	}
	
	public int randomPoint() {
		return HawkRand.randInt(pointLowHigh[0], pointLowHigh[1]);
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getUseItem() {
		return useItem;
	}

	public String getPointRandom() {
		return pointRandom;
	}

	public String getBuyItemPrice() {
		return price;
	}

	public String getRefreshTime() {
		return refreshTime;
	}

	public long getServerMaxPoint() {
		return serverMaxPoint;
	}

	public int getRefreshPoint() {
		return refreshPoint;
	}

	public int getPersonRankSize() {
		return personRankSize;
	}

	public int getExploreBatchLimit() {
		return onceMax;
	}

	public int getNoExplorationTime() {
		return noExplorationTime;
	}

	public int getRefreshTargetId() {
		return refreshTargetId;
	}

	public String getGatherOnce() {
		return gatherOnce;
	}

	public int getServerLogMax() {
		return serverLogMax;
	}

	public int getPlayerLogMax() {
		return playerLogMax;
	}

	public int getDrawMax() {
		return drawMax;
	}

	public int getPlayerDailyGetMax() {
		return playerDailyGetMax;
	}

}
