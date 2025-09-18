package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 装备分解事件
 * 
 * @author Jesse
 *
 */
public class EquipResolveEvent extends ActivityEvent {
	private int cfgId;

	private int lvl;

	private int quality;

	public EquipResolveEvent(){ super(null);}
	public EquipResolveEvent(String playerId, int cfgId, int lvl, int quality) {
		super(playerId);
		this.cfgId = cfgId;
		this.lvl = lvl;
		this.quality = quality;
	}

	public int getCfgId() {
		return cfgId;
	}

	public int getLvl() {
		return lvl;
	}

	public int getQuality() {
		return quality;
	}

}
