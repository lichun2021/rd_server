package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 购买礼包和推送礼包
 * @author jm
 *
 */
public class PackageBuyEvent extends ActivityEvent {
	/**
	 * 购买花费的钻石
	 */
	private int diamond;
	
	public PackageBuyEvent(){ super(null);}
	public PackageBuyEvent(String playerId) {
		super(playerId);
	}
	
	public static PackageBuyEvent valueOf(String playerId, int diamond) {
		PackageBuyEvent pbe = new PackageBuyEvent(playerId);
		pbe.diamond = diamond;
		
		return pbe;
	}

	public int getDiamond() {
		return diamond;
	}

	public void setDiamond(int diamond) {
		this.diamond = diamond;
	}
	
}
