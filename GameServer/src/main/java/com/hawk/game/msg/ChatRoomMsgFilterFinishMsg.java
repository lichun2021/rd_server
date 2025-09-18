package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.pool.HawkObjectPool;
import org.hawk.pool.ObjectPool;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 聊天室聊天信息过滤完成消息
 * 
 * @author lating
 *
 */
@ObjectPool.Declare(minIdle = 128, maxIdle = 2048, timeBetweenEvictionRunsMillis = 300000, minEvictableIdleTimeMillis = 600000)
public class ChatRoomMsgFilterFinishMsg extends HawkMsg {
	/**
	 * 过滤后的消息内容
	 */
	String msgContent;
	/**
	 * 聊天室ID
	 */
	String callbackData;
	
	int resultCode;

	public ChatRoomMsgFilterFinishMsg() {
		super(MsgId.CHATROOM_MSG_FILTER);
	}
	
	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static ChatRoomMsgFilterFinishMsg valueOf(int resultCode, String msgContent, String callbackData) {
		ChatRoomMsgFilterFinishMsg msg = HawkObjectPool.getInstance().borrowObject(ChatRoomMsgFilterFinishMsg.class);
		msg.resultCode = resultCode;
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
	
	public int getResultCode() {
		return resultCode;
	}
	
}
