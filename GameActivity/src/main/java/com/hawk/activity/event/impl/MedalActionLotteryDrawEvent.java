package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**勋章行动抽奖
 * @author Winder
 *
 */
public class MedalActionLotteryDrawEvent extends ActivityEvent {

	private int times;
	
	public MedalActionLotteryDrawEvent(){ super(null);}
	public MedalActionLotteryDrawEvent(String playerId, int times) {
		super(playerId);
		this.times = times;
	}

	public int getTimes() {
		return times;
	}

}
