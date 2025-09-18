package com.hawk.activity.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.pool.HawkObjectPool;
import org.hawk.pool.ObjectPool;

import com.hawk.activity.event.MsgCallBack;

@ObjectPool.Declare(minIdle = 256, maxIdle = 8192, timeBetweenEvictionRunsMillis = 300000, minEvictableIdleTimeMillis = 600000)
public class ActivityCallBackMsg extends HawkMsg {

	private MsgCallBack callBack;
	
	public ActivityCallBackMsg() {
	}
	
	public MsgCallBack getCallBack() {
		return callBack;
	}
	
	public static ActivityCallBackMsg valueOf(MsgCallBack callBack, int msgId) {
		ActivityCallBackMsg msg = HawkObjectPool.getInstance().borrowObject(ActivityCallBackMsg.class);
		msg.setTypeId(msgId);
		msg.callBack = callBack;
		return msg;
	}
}
