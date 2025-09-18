package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 取消科技研究队列消息
 * 
 * @author Jesse
 *
 */
public class CrossTechQueueFinishMsg extends HawkMsg {
	/**
	 * 科技Id
	 */
	int scienceId;

	public int getScienceId() {
		return scienceId;
	}

	public void setScienceId(int scienceId) {
		this.scienceId = scienceId;
	}
	
	public CrossTechQueueFinishMsg() {
		super(MsgId.TECH_QUEUE_FINISH);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static CrossTechQueueFinishMsg valueOf(int scienceId) {
		CrossTechQueueFinishMsg msg = new CrossTechQueueFinishMsg();
		msg.scienceId = scienceId;
		return msg;
	}
}
