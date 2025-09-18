package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**勋章行动抽奖
 * @author Winder
 *
 */
public class HiddenTreasureOpenBoxEvent extends ActivityEvent {

	private int times;
	
	public HiddenTreasureOpenBoxEvent(){ super(null);}
	public HiddenTreasureOpenBoxEvent(String playerId, int times) {
		super(playerId);
		this.times = times;
	}

	public int getTimes() {
		return times;
	}

}
