package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 装备队列完成消息
 * 
 * @author Jesse
 *
 */
public class EquipQueueFinishMsg extends HawkMsg {
	/**
	 * 装备队列itemId
	 */
	String itemId;
	/**
	 * 加速完成标识：0加速完成
	 */
	boolean immediate;
	
	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public boolean getImmediate() {
		return immediate;
	}

	public void setImmediate(boolean immediate) {
		this.immediate = immediate;
	}
	
	public EquipQueueFinishMsg() {
		super(MsgId.CURE_QUEUE_FINISH);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static EquipQueueFinishMsg valueOf(String itemId, boolean immediate) {
		EquipQueueFinishMsg msg = new EquipQueueFinishMsg();
		msg.itemId = itemId;
		msg.immediate = immediate;
		return msg;
	}
}
