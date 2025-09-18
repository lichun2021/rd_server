package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 兵种训练或治疗道具加速消息
 * 
 * @author lating
 *
 */
public class QueueSpeedMsg extends HawkMsg {

	/**
	 * 加速类型
	 */
	private int speedType;

	/**
	 * 消耗加速道具总时间
	 */
	private int speedTime;
	
	public int getSpeedType() {
		return speedType;
	}

	public void setSpeedType(int speedType) {
		this.speedType = speedType;
	}
	
	public int getSpeedTime() {
		return speedTime;
	}

	public void setSpeedTime(int speedTime) {
		this.speedTime = speedTime;
	}
	
	public QueueSpeedMsg() {
		super(MsgId.QUEUE_SPEED);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static QueueSpeedMsg valueOf(int speedType, int speedTime) {
		QueueSpeedMsg msg = new QueueSpeedMsg();
		msg.setSpeedType(speedType);
		msg.setSpeedTime(speedTime);
		return msg;
	}

}
