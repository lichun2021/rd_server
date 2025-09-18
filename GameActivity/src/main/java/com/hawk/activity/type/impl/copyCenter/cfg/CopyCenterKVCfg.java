package com.hawk.activity.type.impl.copyCenter.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/** 十连抽活动全局K-V配置
 * 
 * @author Jesse */
@HawkConfigManager.KVResource(file = "activity/copy_center/copy_center_activity_cfg.xml")
public class CopyCenterKVCfg extends HawkConfigBase {
	// # 服务器开服延时开启活动时间；单位：秒
	private final long serverDelay;// = 0
	// # 使用道具
	private final String costItem;// = 30000_9990001_1
	private final String seniorCostItem;// = 30000_9990003_1;
	// # 道具价格
	private final String itemPrice;// = 10000_1000_168
	private final String seniorItemPrice;// = 10000_1001_560
	private final String exReward;

	// # S级限制次数
	private final int frequencyS;// = 30

	// # SS级限制次数
	private final int frequencySS;// = 20

	public CopyCenterKVCfg() {
		serverDelay = 0;
		itemPrice = "";
		costItem = "";
		seniorCostItem = "";
		seniorItemPrice = "";
		frequencyS = 30;
		frequencySS = 20;
		exReward = "";
	}

	@Override
	protected boolean assemble() {
		return super.assemble();
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public String getCostItem() {
		return costItem;
	}

	public String getItemPrice() {
		return itemPrice;
	}

	public String getSeniorCostItem() {
		return seniorCostItem;
	}

	public String getSeniorItemPrice() {
		return seniorItemPrice;
	}

	public int getFrequencyS() {
		return frequencyS;
	}

	public int getFrequencySS() {
		return frequencySS;
	}

	public String getExReward() {
		return exReward;
	}

	@Override
	protected boolean checkValid() {
		// if (ConfigChecker.getDefaultChecker().checkAwardsValid(costItem)) {
		// throw new
		// InvalidParameterException(String.format("copy_center_activity_cfg
		// error, costItem: %s", costItem));
		// }
		// if (ConfigChecker.getDefaultChecker().checkAwardsValid(itemPrice)) {
		// throw new
		// InvalidParameterException(String.format("copy_center_activity_cfg
		// error, itemPrice: %s", itemPrice));
		// }
		return super.checkValid();
	}

}