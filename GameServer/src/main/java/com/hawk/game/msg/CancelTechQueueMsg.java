package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 取消科技研究队列消息
 * 
 * @author Jesse
 *
 */
public class CancelTechQueueMsg extends HawkMsg {
	/**
	 * 科技Id
	 */
	int techId;
	/**
	 * 返还资源
	 */
	String cancelBackRes;

	public int getTechId() {
		return techId;
	}

	public void setTechId(int techId) {
		this.techId = techId;
	}

	public String getCancelBackRes() {
		return cancelBackRes;
	}

	public void setCancelBackRes(String cancelBackRes) {
		this.cancelBackRes = cancelBackRes;
	}
	
	public CancelTechQueueMsg() {
		super(MsgId.TECH_QUEUE_CANCEL);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static CancelTechQueueMsg valueOf(int techId, String cancelBackRes) {
		CancelTechQueueMsg msg = new CancelTechQueueMsg();
		msg.techId = techId;
		msg.cancelBackRes = cancelBackRes;
		return msg;
	}
}
