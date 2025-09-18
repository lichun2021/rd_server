package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 建筑队列取消消息
 * 
 * @author lating
 *
 */
public class BuildingQueueCancelMsg extends HawkMsg {
	/**
	 * 建筑Id
	 */
	String itemId;
	/**
	 * 队列状态
	 */
	int status;
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCancelBackRes() {
		return cancelBackRes;
	}

	public void setCancelBackRes(String cancelBackRes) {
		this.cancelBackRes = cancelBackRes;
	}

	public BuildingQueueCancelMsg() {
		super(MsgId.BUILDING_QUEUE_CANCEL);
	}
	
	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static BuildingQueueCancelMsg valueOf(String itemId, int status, String cancelBackRes) {
		BuildingQueueCancelMsg msg = new BuildingQueueCancelMsg();
		msg.itemId = itemId;
		if(itemId.indexOf("_") > 0) {
			String[] items = itemId.split("_");
			msg.itemId = items[0];
		}
		msg.status = status;
		msg.cancelBackRes = cancelBackRes;
		return msg;
	}
}
