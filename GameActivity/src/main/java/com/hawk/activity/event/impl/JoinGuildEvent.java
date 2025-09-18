package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 加入联盟事件（包括创建联盟）
 * 
 * @author lating
 *
 */
public class JoinGuildEvent extends ActivityEvent {

	public JoinGuildEvent(){ super(null);}
	public JoinGuildEvent(String playerId) {
		super(playerId);
	}

}
