package com.hawk.activity.event.impl;


import com.hawk.activity.event.ActivityEvent;

/**
 * 先锋豪礼购买
 * 
 * @author RickMei
 *
 */
public class PioneerGifBuyTimesEvent extends ActivityEvent {

	private int buyCfgId = 0;

	public PioneerGifBuyTimesEvent(){ super(null);}
	public PioneerGifBuyTimesEvent(String playerId, int cfgId) {
		super(playerId);
		this.buyCfgId = cfgId;
	}

	static public PioneerGifBuyTimesEvent valueOf(String playerId, int cfgId) {
		PioneerGifBuyTimesEvent event = new PioneerGifBuyTimesEvent(playerId, cfgId);
		event.buyCfgId = cfgId;
		return event;
	}

	public int getBuyCfgId() {
		return buyCfgId;
	}
}
