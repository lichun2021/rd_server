package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.pool.HawkObjectPool;
import org.hawk.pool.ObjectPool;

import com.hawk.gamelib.GameConst.MsgId;

@ObjectPool.Declare(minIdle = 128, maxIdle = 2048, timeBetweenEvictionRunsMillis = 300000, minEvictableIdleTimeMillis = 600000)
public class SendRedPacketMsg extends HawkMsg {
	
	/**
	 * 过滤后的消息内容
	 */
	String msgContent;

	String callbackData;

	public SendRedPacketMsg() {
		super(MsgId.RED_PACKET_MSG_FILTER);
	}
	
	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static SendRedPacketMsg valueOf(String msgContent, String callbackData) {
		SendRedPacketMsg msg = HawkObjectPool.getInstance().borrowObject(SendRedPacketMsg.class);
		msg.msgContent = msgContent;
		msg.callbackData = callbackData;
		return msg;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public String getCallbackData() {
		return callbackData;
	}

	public void setCallbackData(String callbackData) {
		this.callbackData = callbackData;
	}
}
