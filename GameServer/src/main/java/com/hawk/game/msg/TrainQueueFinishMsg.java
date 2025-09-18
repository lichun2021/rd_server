package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 兵种训练队列完成消息
 * 
 * @author lating
 *
 */
public class TrainQueueFinishMsg extends HawkMsg {
	/**
	 * 兵种Id
	 */
	String itemId;
	/**
	 * 是否加速完成
	 */
	boolean isImmediate;
	
	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public boolean isImmediate() {
		return isImmediate;
	}

	public void setImmediate(boolean isImmediate) {
		this.isImmediate = isImmediate;
	}
	
	public TrainQueueFinishMsg() {
		super(MsgId.TRAIN_QUEUE_FINISH);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static TrainQueueFinishMsg valueOf(String itemId, boolean isImmediate) {
		TrainQueueFinishMsg msg = new TrainQueueFinishMsg();
		if(itemId.indexOf("_") > 0) {
			msg.itemId = itemId.split("_")[1];
		} else {
			msg.itemId = itemId;
		}
		msg.isImmediate = isImmediate;
		return msg;
	}
}
