package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.util.GsConst.TimerEventEnum;

public class TimerEventMsg extends HawkMsg {
	
	/**
	 * 标识是几点钟的类型, 该值不可修改
	 */
	private TimerEventEnum eventEnum;
	
	public TimerEventEnum getEventEnum() {
		return eventEnum;
	}

	public static TimerEventMsg valueOf(TimerEventEnum enums) {
		TimerEventMsg msg = new TimerEventMsg();
		msg.eventEnum = enums;
		
		return msg;
	}
	 
}
