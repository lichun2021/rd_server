package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 伤兵治疗队列取消消息
 * 
 * @author lating
 *
 */
public class CureQueueCancelMsg extends HawkMsg {
	/**
	 * 取消返还资源
	 */
	String cancelBackRes;
	
	public String getCancelBackRes() {
		return cancelBackRes;
	}

	public void setCancelBackRes(String cancelBackRes) {
		this.cancelBackRes = cancelBackRes;
	}
	
	public CureQueueCancelMsg() {
		super(MsgId.CURE_QUEUE_CANCEL);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static CureQueueCancelMsg valueOf(String cancelBackRes) {
		CureQueueCancelMsg msg = new CureQueueCancelMsg();
		msg.cancelBackRes = cancelBackRes;
		return msg;
	}
}
