package com.hawk.game.nation.wearhouse;

import java.util.HashMap;
import java.util.Map;

/**
 * 国家商店信息
 * 
 * @author lating
 *
 */
public class NationShopRealtimeInfo {
	private long refreshTime;
	private Map<Integer, Integer> shopItemCount;
	
	public NationShopRealtimeInfo() {
		shopItemCount = new HashMap<Integer, Integer>();
	}
	
	public static NationShopRealtimeInfo valueOf(long time) {
		NationShopRealtimeInfo shopInfo = new NationShopRealtimeInfo();
		shopInfo.setRefreshTime(time);
		return shopInfo;
	}

	public long getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}

	public Map<Integer, Integer> getShopItemCount() {
		return shopItemCount;
	}

}
