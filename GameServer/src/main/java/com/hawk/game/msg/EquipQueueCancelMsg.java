package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 装备队列完成消息
 * 
 * @author Jesse
 *
 */
public class EquipQueueCancelMsg extends HawkMsg {
	/**
	 * 队列itemId
	 */
	String itemId;
	
	/**
	 * 消耗返还
	 */
	String resReturn;
	
	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	
	public String getResReturn() {
		return resReturn;
	}

	public void setResReturn(String resReturn) {
		this.resReturn = resReturn;
	}

	public EquipQueueCancelMsg() {
		super(MsgId.CURE_QUEUE_FINISH);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static EquipQueueCancelMsg valueOf(String itemId, String resReturn) {
		EquipQueueCancelMsg msg = new EquipQueueCancelMsg();
		msg.itemId = itemId;
		msg.resReturn = resReturn;
		return msg;
	}
}
