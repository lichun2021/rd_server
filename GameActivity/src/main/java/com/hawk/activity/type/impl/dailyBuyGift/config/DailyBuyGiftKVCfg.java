package com.hawk.activity.type.impl.dailyBuyGift.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

/**
 *  中部养成计划
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/daiy_buy_gift/daiy_buy_gift_cfg.xml")
public class DailyBuyGiftKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	// 每日游戏轮次
	private final int refreshDays;
	
	private final String recordGiftItems;
	
	private final String timeLimit;
	
	private List<Integer> recordGiftItemList = new ArrayList<>();
	
	private long timeLimitValue;
	
	public DailyBuyGiftKVCfg() {
		serverDelay = 0;
		refreshDays = 1;
		recordGiftItems = "";
		timeLimit= "";
	}
	
	
	@Override
	protected boolean assemble() {
		if(!HawkOSOperator.isEmptyString(recordGiftItems)){
			List<Integer> recordGiftItemListTemp = new ArrayList<>();
			String[] arr = recordGiftItems.split(",");
			for(String str : arr){
				recordGiftItemListTemp.add(Integer.parseInt(str));
			}
			this.recordGiftItemList = recordGiftItemListTemp;
		}
		if(!HawkOSOperator.isEmptyString(timeLimit)){
			timeLimitValue = HawkTime.parseTime(timeLimit);
		}
		
		return true;
	}


	public long getServerDelay() {
		return serverDelay  * 1000l;
	}


	public int getRefreshDays() {
		return refreshDays;
	}
	
	public List<Integer> getRecordGiftItemList() {
		return recordGiftItemList;
	}
	
	public long getTimeLimitValue() {
		return timeLimitValue;
	}

	
}