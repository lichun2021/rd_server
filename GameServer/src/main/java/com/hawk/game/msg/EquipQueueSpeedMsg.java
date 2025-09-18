package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 装备研究道具加速消息
 * 
 * @author lating
 *
 */
public class EquipQueueSpeedMsg extends HawkMsg {

	/**
	 * 消耗加速道具总时间
	 */
	private int speedTime;
	/**
	 * 升到下一级需要的道具数量
	 */
	private int nextNeedItemNum;
	/**
	 * 升级需要的道具ID
	 */
	private int needItemId;
	
	public int getSpeedTime() {
		return speedTime;
	}

	public void setSpeedTime(int speedTime) {
		this.speedTime = speedTime;
	}
	
	public int getNextNeedItemNum() {
		return nextNeedItemNum;
	}

	public void setNextNeedItemNum(int nextNeedItemNum) {
		this.nextNeedItemNum = nextNeedItemNum;
	}

	public int getNeedItemId() {
		return needItemId;
	}

	public void setNeedItemId(int needItemId) {
		this.needItemId = needItemId;
	}

	public EquipQueueSpeedMsg() {
		super(MsgId.EQUIP_RESEARCH);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static EquipQueueSpeedMsg valueOf(int speedTime, int itemId, int needItemNum) {
		EquipQueueSpeedMsg msg = new EquipQueueSpeedMsg();
		msg.setSpeedTime(speedTime);
		msg.setNeedItemId(itemId);
		msg.setNextNeedItemNum(needItemNum);
		return msg;
	}

}
