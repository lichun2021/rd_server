package com.hawk.activity.type.impl.christmaswar.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 
 * @author jm
 *
 */
@HawkConfigManager.KVResource(file = "activity/christmas_war/christmas_war_cfg.xml")
public class ChristmasWarKVCfg extends HawkConfigBase {
	/**
	 * 延迟
	 */
	private final int serverDelay;
	/**
	 * 每日是否重置
	 */
	private final boolean isDailyReset;
	/**
	 * 个人伤害排行限制
	 */
	private final int selfRankLimit;
	/**
	 * 工会排行限制.
	 */
	private final int guildRankLimit;
	/**
	 * 个人击杀排行.
	 */
	private final int killRankLimit;
	/**
	 * 排行周期.
	 */
	private final int rankPeriod;
	/**
	 * 显示的最大杀敌数.
	 */
	private final int showMaxNum;
	/**
	 * 注水
	 */
	private final String injectWater;
	/**
	 * 注水.
	 */
	private List<int[]> injectWaterList;
	
	private static ChristmasWarKVCfg instance;
	
	public static ChristmasWarKVCfg getInstance() {
		return instance;
	}
	
	public ChristmasWarKVCfg() {
		this.serverDelay = 0;
		this.isDailyReset = false;
		this.selfRankLimit = 100;
		this.guildRankLimit = 10;
		this.killRankLimit = 100;
		this.rankPeriod = 60000;
		
		this.showMaxNum = 5000;
		this.injectWater = "";
		
		instance = this;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public boolean isDailyReset() {
		return isDailyReset;
	}

	public int getSelfRankLimit() {
		return selfRankLimit;
	}

	public int getGuildRankLimit() {
		return guildRankLimit;
	}

	public int getKillRankLimit() {
		return killRankLimit;
	}

	public int getRankPeriod() {
		return rankPeriod;
	}

	
	
	@Override
	public boolean assemble() {
		if (HawkOSOperator.isEmptyString(injectWater)) {
			injectWaterList = Collections.synchronizedList(new ArrayList<>());
		} else {
			List<int[]> intArrayList = new ArrayList<>();
			String[] itemStringArray = injectWater.split(",");
			for (String itemString : itemStringArray) {
				String[] itemArray = itemString.split("_");
				int[] intArray = new int[itemArray.length];
				for (int i = 0; i < itemArray.length; i ++) {
					intArray[i] = Integer.parseInt(itemArray[i]);
				}
				
				intArrayList.add(intArray);
			}
			
			this.injectWaterList = Collections.synchronizedList(intArrayList);
		}
		
		return true;
	}

	public int getShowMaxNum() {
		return showMaxNum;
	}

	public List<int[]> getInjectWaterList() {
		return injectWaterList;
	}

	public void setInjectWaterList(List<int[]> injectWaterList) {
		this.injectWaterList = injectWaterList;
	}

}

