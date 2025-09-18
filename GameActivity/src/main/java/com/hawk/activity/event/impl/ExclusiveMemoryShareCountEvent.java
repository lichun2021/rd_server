package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 豪礼派送累计登录事件
 * @author yang.rao
 *
 */
public class ExclusiveMemoryShareCountEvent extends ActivityEvent {

	/** 累计登录天数*/
	private int shareDays;
	
	public ExclusiveMemoryShareCountEvent(){ super(null);}
	public ExclusiveMemoryShareCountEvent(String playerId, int shareDays) {
		super(playerId);
		this.shareDays = shareDays;
	}

	public int getShareDays() {
		return shareDays;
	}
}
