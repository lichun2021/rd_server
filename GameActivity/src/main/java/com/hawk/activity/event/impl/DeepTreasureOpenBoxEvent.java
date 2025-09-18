package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**秘藏抽奖
 * @author zhy
 *
 */
public class DeepTreasureOpenBoxEvent extends ActivityEvent {

	private int times;

	public DeepTreasureOpenBoxEvent(){ super(null);}
	public DeepTreasureOpenBoxEvent(String playerId, int times) {
		super(playerId);
		this.times = times;
	}

	public int getTimes() {
		return times;
	}

}
