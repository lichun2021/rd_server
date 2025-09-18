package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 队列被盟友帮助
 * 
 * @author Jesse
 *
 */
public class QueueBeHelpedMsg extends HawkMsg {
	/**
	 * 队列Id
	 */
	String queueId;
	
	/**
	 * 队列减少时间
	 */
	long assistTime;
	
	public String getQueueId() {
		return queueId;
	}

	public long getAssistTime() {
		return assistTime;
	}

	public QueueBeHelpedMsg() {
		super(MsgId.QUEUE_BE_HELPED);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static QueueBeHelpedMsg valueOf(String queueId, long assistTime) {
		QueueBeHelpedMsg msg = new QueueBeHelpedMsg();
		msg.queueId = queueId;
		msg.assistTime = assistTime;
		return msg;
	}
}
