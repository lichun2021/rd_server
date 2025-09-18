package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 第X次将装备等级强化至Y级
 * @author Golden
 *
 */
public class ArmourIntensifyTimesMsg extends HawkMsg {
	int level;
	int times;
	
	public static ArmourIntensifyTimesMsg valueOf(int times, int level) {
		ArmourIntensifyTimesMsg msg = new ArmourIntensifyTimesMsg();
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
