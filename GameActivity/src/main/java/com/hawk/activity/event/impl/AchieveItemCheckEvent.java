package com.hawk.activity.event.impl;

import java.util.Collection;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 成就完成检测事件
 * @author PhilChen
 *
 */
public class AchieveItemCheckEvent extends ActivityEvent {

	/** 成就项列表*/
	private Collection<AchieveItem> items;

	public AchieveItemCheckEvent(){ super(null);}
	public AchieveItemCheckEvent(String playerId, Collection<AchieveItem> items) {
		super(playerId);
		this.items = items;
	}

	public Collection<AchieveItem> getItems() {
		return items;
	}

}
