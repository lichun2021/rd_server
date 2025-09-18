package com.hawk.activity.event.impl;

import java.util.Set;

import com.hawk.activity.event.ActivityEvent;

/**泰伯利亚联赛竞猜发奖
 * @author Winder
 *
 */
public class TblyGuessSendRewardEvent extends ActivityEvent{

	private int mark;
	private Set<String> winGuilds;
	
	public TblyGuessSendRewardEvent(){ super(null);}
	public TblyGuessSendRewardEvent(int mark, Set<String> winGuilds) {
		super("");
		this.mark = mark;
		this.winGuilds = winGuilds;
	}

	public int getMark() {
		return mark;
	}

	public Set<String> getWinGuilds() {
		return winGuilds;
	}


}
