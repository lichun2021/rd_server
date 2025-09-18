package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.game.protocol.Hero.DailyShareType;

/***
 * 每日任务分享事件
 * @author yang.rao
 *
 */
public class DailyMissionShareEvent extends ActivityEvent {

	/** 分享类型 **/
	private DailyShareType shareType;
	
	public DailyMissionShareEvent(){ super(null);}
	public DailyMissionShareEvent(String playerId, DailyShareType shareType) {
		super(playerId);
		this.shareType = shareType;
	}

	public DailyShareType getShareType() {
		return shareType;
	}
}
