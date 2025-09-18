package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.pool.HawkObjectPool;
import org.hawk.pool.ObjectPool;

import com.hawk.activity.event.speciality.CrossActivityEvent;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 跨服活动任务消息
 * 
 * @author Jesse
 *
 */
@ObjectPool.Declare(minIdle = 128, maxIdle = 2048, timeBetweenEvictionRunsMillis = 300000, minEvictableIdleTimeMillis = 600000)
public class CrossActivityMsg extends HawkMsg {

	private CrossActivityEvent event;

	public CrossActivityMsg() {
		super(MsgId.CROSS_TASK_MSG);
	}

	public CrossActivityEvent getEvent() {
		return event;
	}

	public void setEvent(CrossActivityEvent event) {
		this.event = event;
	}

	public static CrossActivityMsg valueOf(CrossActivityEvent event) {
		CrossActivityMsg msg = HawkObjectPool.getInstance().borrowObject(CrossActivityMsg.class);
		msg.event = event;
		return msg;
	}
}
