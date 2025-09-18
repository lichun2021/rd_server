package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 双旦活动联盟购买礼包人数成就进度通知事件
 * 
 * @author lating
 *
 */
public class GuildPayGiftAchieveNotifyEvent extends ActivityEvent {
	public GuildPayGiftAchieveNotifyEvent(){ super(null);}
	public GuildPayGiftAchieveNotifyEvent(String playerId) {
		super(playerId);
	}

}
