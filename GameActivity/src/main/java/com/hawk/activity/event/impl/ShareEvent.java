package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.game.protocol.Hero.DailyShareType;

/***
 * 任务分享事件
 * @author jesse
 *
 */
public class ShareEvent extends ActivityEvent {

	/** 分享类型 **/
	private DailyShareType shareType;

	public ShareEvent(){ super(null);}
	public ShareEvent(String playerId, DailyShareType shareType) {
		super(playerId);
		this.shareType = shareType;
	}

	public DailyShareType getShareType() {
		return shareType;
	}
}
