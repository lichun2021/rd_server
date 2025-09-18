package com.hawk.activity.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.pool.HawkObjectPool;
import org.hawk.pool.ObjectPool;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 异步执行活动事件
 * 
 * @author PhilChen
 *
 */
@ObjectPool.Declare(minIdle = 256, maxIdle = 8192, timeBetweenEvictionRunsMillis = 300000, minEvictableIdleTimeMillis = 600000)
public class AsyncActivityEventMsg extends HawkMsg {

	private ActivityEvent event;

	public AsyncActivityEventMsg() {
		super(MsgId.ASYNC_ACTIVITY_EVENT);
	}

	public ActivityEvent getEvent() {
		return event;
	}
	
	public static AsyncActivityEventMsg valueOf(ActivityEvent event) {
		AsyncActivityEventMsg msg = HawkObjectPool.getInstance().borrowObject(AsyncActivityEventMsg.class);
		msg.event = event;
		return msg;
	}
}
