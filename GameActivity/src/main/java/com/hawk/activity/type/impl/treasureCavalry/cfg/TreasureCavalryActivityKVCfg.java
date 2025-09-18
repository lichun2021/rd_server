package com.hawk.activity.type.impl.treasureCavalry.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;

/**
 * 十连抽活动全局K-V配置
 * 
 * @author Jesse
 */
@HawkConfigManager.KVResource(file = "activity/treasure_cavalry/treasure_cavalry_activity_cfg.xml")
public class TreasureCavalryActivityKVCfg extends HawkConfigBase {
	// # 服务器开服延时开启活动时间；单位：秒
	private final long serverDelay;// = 0
	// # 倍数道具
	private final String multipleItem;// = 123456789
	// # 翻牌子消耗
	private final String treasureCost;// = 10000_1010_0,10000_1010_1,10000_1010_2,10000_1010_3,10000_1010_4,10000_1010_5,10000_1010_6,10000_1010_7,10000_1010_8,10000_1010_9
	// # 自动刷新时间(秒) -->废弃, 改为0点刷新
	private final int refreshReset;// = 36000
	// # 刷新次数, 自动刷新时间到重置
	private final int maxRefresh;// = 5
	// # 刷新消耗
	private final String refreshCost;// = 10000_1010_0,10000_1010_1,10000_1010_2,10000_1010_3,10000_1010_4,10000_1010_5,10000_1010_6,10000_1010_7,10000_1010_8,10000_1010_9
	// # 道具价格
	private final String itemOnecePrice;// = 10000_1000_56

	// # 购买1次获得固定奖励
	private final String extReward;// = 30000_840172_1

	private List<String> treasureCostList;
	private List<String> refreshCostList;

	public TreasureCavalryActivityKVCfg() {
		serverDelay = 0;
		multipleItem = "1150068,1150069,1150070";
		treasureCost = "10000_1010_0,10000_1010_1,10000_1010_2,10000_1010_3,10000_1010_4,10000_1010_5,10000_1010_6,10000_1010_7,10000_1010_8,10000_1010_9";
		refreshReset = 36000;
		maxRefresh = 5;
		refreshCost = "10000_1010_0,10000_1010_1,10000_1010_2,10000_1010_3,10000_1010_4,10000_1010_5,10000_1010_6,10000_1010_7,10000_1010_8,10000_1010_9";
		itemOnecePrice = "10000_1000_56";
		extReward = "30000_840172_1";

	}

	@Override
	protected boolean assemble() {
		treasureCostList = Splitter.on(",").omitEmptyStrings().splitToList(treasureCost);
		refreshCostList = Splitter.on(",").omitEmptyStrings().splitToList(refreshCost);
		return super.assemble();
	}

	/** 是否倍数道具 */
	public boolean isMultipleItem(int itemId) {
		return multipleItem.contains(itemId + "");
	}

	/** 翻牌消耗 */
	public String getTreasureCost(int index) {
		index = Math.min(index, treasureCostList.size() - 1);
		return treasureCostList.get(index);
	}

	/** 刷新消耗 */
	public String getRefreshCost(int index) {
		index = Math.min(index, refreshCostList.size() - 1);
		return refreshCostList.get(index);
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}

	public String getMultipleItem() {
		return multipleItem;
	}

	public String getItemOnecePrice() {
		return itemOnecePrice;
	}

	public String getExtReward() {
		return extReward;
	}

//	public int getRefreshReset() {
//		return refreshReset;
//	}

	public int getMaxRefresh() {
		return maxRefresh;
	}

	public String getTreasureCost() {
		return treasureCost;
	}

	public String getRefreshCost() {
		return refreshCost;
	}

}