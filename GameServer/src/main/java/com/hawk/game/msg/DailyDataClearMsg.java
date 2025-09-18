package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 每日数据重置消息
 * @author golden
 *
 */
public class DailyDataClearMsg extends HawkMsg {
	
	public DailyDataClearMsg() {
		super(MsgId.CURE_QUEUE_FINISH);
	}
	
	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static DailyDataClearMsg valueOf() {
		DailyDataClearMsg msg = new DailyDataClearMsg();
		return msg;
	}
}
