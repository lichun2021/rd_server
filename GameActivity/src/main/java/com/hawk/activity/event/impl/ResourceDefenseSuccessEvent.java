package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 资源被偷取防御成功事件
 * @author hf
 */
public class ResourceDefenseSuccessEvent extends ActivityEvent {

	public ResourceDefenseSuccessEvent(){ super(null);}
	public ResourceDefenseSuccessEvent(String playerId) {
		super(playerId);

	}
}
