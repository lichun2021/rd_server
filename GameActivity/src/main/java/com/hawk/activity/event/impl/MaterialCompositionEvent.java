package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 装备材料合成事件
 * 
 * @author lating
 *
 */
public class MaterialCompositionEvent extends ActivityEvent {
	
	public MaterialCompositionEvent(){ super(null);}
	public MaterialCompositionEvent(String playerId) {
		super(playerId);
	}
}
