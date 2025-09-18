package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 合服比拼活动刷新数据事件
 * @author lating
 */
public class MergeCompeteRefreshEvent extends ActivityEvent {

	private boolean giftReward;
	private boolean guildPower;
	
	public MergeCompeteRefreshEvent(){ super(null);}
	public MergeCompeteRefreshEvent(String playerId, boolean giftReward, boolean guildPower) {
		super(playerId);
		this.giftReward = giftReward;
		this.guildPower = guildPower;
	}

	public boolean isGiftReward() {
		return giftReward;
	}

	public boolean isGuildPower() {
		return guildPower;
	}
}
