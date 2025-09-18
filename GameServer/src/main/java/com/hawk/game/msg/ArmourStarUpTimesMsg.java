package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 第X次将装备泰晶等级强化至Y级
 * @author Golden
 *
 */
public class ArmourStarUpTimesMsg extends HawkMsg {
	int level;
	int times;
	
	public static ArmourStarUpTimesMsg valueOf(int times, int level) {
		ArmourStarUpTimesMsg msg = new ArmourStarUpTimesMsg();
		msg.times = times;
		msg.level = level;
		return msg;
	}

	public int getTimes() {
		return times;
	}
	
	public int getLevel() {
		return level;
	}
}
