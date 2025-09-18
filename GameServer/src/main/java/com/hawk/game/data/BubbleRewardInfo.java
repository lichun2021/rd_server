package com.hawk.game.data;

import org.hawk.os.HawkTime;

import com.hawk.game.item.ItemInfo;

/**
 * 冒泡奖励信息
 * 
 * @author lating
 *
 */
public class BubbleRewardInfo {
	/**
	 * 冒泡类型   - 1兵种冒泡，2建筑冒泡
	 */
	private int type;
	/**
	 * 当日上次领取时间
	 */
	private long lastTime;
	/**
	 * 当日下次可领取的奖励
	 */
	private ItemInfo nextRewardItem;
	/**
	 * 当日已领取次数
	 */
	private int gotTimes;
	
	public BubbleRewardInfo() {
		
	}
	
	public BubbleRewardInfo(int type, ItemInfo item) {
		this.type = type;
		this.lastTime = HawkTime.getMillisecond();
		this.gotTimes = 0;
		this.nextRewardItem = item;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public long getLastTime() {
		return lastTime;
	}
	
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
	
	public ItemInfo getNextRewardItem() {
		return nextRewardItem;
	}
	
	public void setNextRewardItem(ItemInfo nextRewardItem) {
		this.nextRewardItem = nextRewardItem;
	}
	
	public int getGotTimes() {
		return gotTimes;
	}
	
	public void setGotTimes(int gotTimes) {
		this.gotTimes = gotTimes;
	}
	
}
