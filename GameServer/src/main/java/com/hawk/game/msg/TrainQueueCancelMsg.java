package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 兵种训练队列取消消息
 * 
 * @author lating
 *
 */
public class TrainQueueCancelMsg extends HawkMsg {
	/**
	 * 兵种Id
	 */
	String itemId;
	/**
	 * 协议号
	 */
	int protoType;
	/**
	 * 取消返还资源
	 */
	String cancelBackRes;
	
	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public int getProtoType() {
		return protoType;
	}

	public void setProtoType(int protoType) {
		this.protoType = protoType;
	}

	public String getCancelBackRes() {
		return cancelBackRes;
	}

	public void setCancelBackRes(String cancelBackRes) {
		this.cancelBackRes = cancelBackRes;
	}
	
	public TrainQueueCancelMsg() {
		super(MsgId.TRAIN_QUEUE_CANCEL);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static TrainQueueCancelMsg valueOf(String itemId, int protoType, String cancelBackRes) {
		TrainQueueCancelMsg msg = new TrainQueueCancelMsg();
		if(itemId.indexOf("_") > 0) {
			msg.itemId = itemId.split("_")[1];
		} else {
			msg.itemId = itemId;
		}
		msg.protoType = protoType;
		msg.cancelBackRes = cancelBackRes;
		return msg;
	}
}
