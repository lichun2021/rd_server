package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 伤兵治疗队列完成消息
 * 
 * @author lating
 *
 */
public class CureQueueFinishMsg extends HawkMsg {
	/**
	 * 兵种id
	 */
	String itemId;
	/**
	 * 加速完成标识：0加速完成
	 */
	int immediate;
	
	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public int getImmediate() {
		return immediate;
	}

	public void setImmediate(int immediate) {
		this.immediate = immediate;
	}
	
	public CureQueueFinishMsg() {
		super(MsgId.CURE_QUEUE_FINISH);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static CureQueueFinishMsg valueOf(String itemId, int immediate) {
		CureQueueFinishMsg msg = new CureQueueFinishMsg();
		msg.itemId = itemId;
		msg.immediate = immediate;
		return msg;
	}
}
