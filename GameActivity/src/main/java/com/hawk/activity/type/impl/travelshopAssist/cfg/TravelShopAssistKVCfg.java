package com.hawk.activity.type.impl.travelshopAssist.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 特惠惊喜活动K-V配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/travel_shop_assist/travel_shop_assist_cfg.xml")
public class TravelShopAssistKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	
	/** 是否每日重置(宝箱除外)*/
	private final int isDailyReset;
	
	
	private final int freeRefreshTimes;
	
	
	/** 黑市商店助力庆典活动中每日系统刷新时间整点  9_12_15_18_20_22*/
	private final String travelShopRefreshTime;

	/** 系统刷新后，重置的水晶刷新次数和消耗  = 25_50_75_100_125_150_175_200*/
	private final String travelShopCrystalRefreshCost;
	
	
	private int[] travelShopRefreshTimeArr;
	
	private int[] travelShopCrystalRefreshCostArr;
	
	public TravelShopAssistKVCfg() {
		serverDelay = 0;
		isDailyReset = 0;
		freeRefreshTimes = 0;
		travelShopRefreshTime = "";
		travelShopCrystalRefreshCost = "";
	}
	
	
	@Override
	protected boolean assemble() {
		
		if (!HawkOSOperator.isEmptyString(travelShopCrystalRefreshCost)) {
			String[] costArray = travelShopCrystalRefreshCost.split("_");
			int[] intCostArray = new int[costArray.length];			
			for (int i = 0; i < costArray.length; i++) {
				intCostArray[i] =  Integer.parseInt(costArray[i]);
			}
			
			travelShopCrystalRefreshCostArr = intCostArray;
		} else {
			return false;
		}
		
		if (!HawkOSOperator.isEmptyString(travelShopRefreshTime)) {
			String[] costArray = travelShopRefreshTime.split("_");
			int[] intCostArray = new int[costArray.length];			
			for (int i = 0; i < costArray.length; i++) {
				intCostArray[i] =  Integer.parseInt(costArray[i]);
			}
			travelShopRefreshTimeArr = intCostArray;
		} else {
			return false;
		}
		return true;
	}
	
	
	
	
	public int[] getTravelShopRefreshTimeArr() {
		return travelShopRefreshTimeArr;
	}


	public void setTravelShopRefreshTimeArr(int[] travelShopRefreshTimeArr) {
		this.travelShopRefreshTimeArr = travelShopRefreshTimeArr;
	}


	public int[] getTravelShopCrystalRefreshCostArr() {
		return travelShopCrystalRefreshCostArr;
	}


	public void setTravelShopCrystalRefreshCostArr(int[] travelShopCrystalRefreshCostArr) {
		this.travelShopCrystalRefreshCostArr = travelShopCrystalRefreshCostArr;
	}


	public int getMaxTravelShopInActivtyRefreshTimes() {
		return freeRefreshTimes + travelShopCrystalRefreshCostArr.length;
	}

	
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public boolean isDailyReset() {
		return isDailyReset == 1;
	}

	public int getIsDailyReset() {
		return isDailyReset;
	}

	public int getFreeRefreshTimes() {
		return freeRefreshTimes;
	}

	
	public String getTravelShopRefreshTime() {
		return travelShopRefreshTime;
	}

	public String getTravelShopCrystalRefreshCost() {
		return travelShopCrystalRefreshCost;
	}

	
	

	
}