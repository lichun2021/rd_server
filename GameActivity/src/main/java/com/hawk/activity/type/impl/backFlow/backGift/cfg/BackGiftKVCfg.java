package com.hawk.activity.type.impl.backFlow.backGift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 回归大礼活动配置
 * 
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/back_gift_activity/back_gift_activity_cfg.xml")
public class BackGiftKVCfg extends HawkConfigBase {
	
	//活动解锁条件
	private final int lostTime;
	
	//每日刷新次数
	private final int resetLimit;

	//每日刷新CD 单位：秒
	private final int resetLimitCd;

	//活动持续天数 单位：秒
	private final int duration;

	//每日免费次数
	private final int costFree;
	
	//抽奖消耗
	private final String lotteryCostItem;
	
	//免费抽奖CD
	private final int lotteryLimitCd;

	public BackGiftKVCfg() {
		lostTime =0;
		resetLimit = 0;
		resetLimitCd = 0;
		duration = 0;
		costFree = 0;
		lotteryCostItem = "";
		lotteryLimitCd = 0;
	}

	public int getLostTime() {
		return lostTime;
	}

	public int getResetLimit() {
		return resetLimit;
	}

	public int getResetLimitCd() {
		return resetLimitCd;
	}

	public int getDuration() {
		return duration * 1000;
	}

	public int getCostFree() {
		return costFree;
	}

	public String getLotteryCostItem() {
		return lotteryCostItem;
	}

	public int getLotteryLimitCd() {
		return lotteryLimitCd;
	}

	
	
	
	
	
	
	
	
}
