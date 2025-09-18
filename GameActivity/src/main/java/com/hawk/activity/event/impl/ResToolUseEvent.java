package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 资源道具使用事件
 * 
 * @author lating
 *
 */
public class ResToolUseEvent extends ActivityEvent {
	
	private int itemCount;
	
	public ResToolUseEvent(){ super(null);}
	public ResToolUseEvent(String playerId, int itemCount) {
		super(playerId);
		this.itemCount = itemCount;
	}
	
	public int getItemCount() {
		return itemCount;
	}
}
