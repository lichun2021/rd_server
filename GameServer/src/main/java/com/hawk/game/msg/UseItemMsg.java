package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消耗指定道具消息
 * 
 * @author lating
 *
 */
public class UseItemMsg extends HawkMsg {

	/**
	 * 道具ID
	 */
	private int itemId;
	/**
	 * 使用数量
	 */
	private int count;
	
	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public UseItemMsg() {
		super(MsgId.ITEM_CONSUME);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static UseItemMsg valueOf(int itemId, int count) {
		UseItemMsg msg = new UseItemMsg();
		msg.setItemId(itemId);
		msg.setCount(count);
		return msg;
	}

}
