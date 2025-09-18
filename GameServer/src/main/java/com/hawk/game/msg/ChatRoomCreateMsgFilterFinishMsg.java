package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.pool.HawkObjectPool;
import org.hawk.pool.ObjectPool;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 聊天室创建信息过滤完成消息
 * 
 * @author lating
 *
 */
@ObjectPool.Declare(minIdle = 128, maxIdle = 2048, timeBetweenEvictionRunsMillis = 300000, minEvictableIdleTimeMillis = 600000)
public class ChatRoomCreateMsgFilterFinishMsg extends HawkMsg {
	/**
	 * 过滤后的消息内容
	 */
	String msgContent;
	/**
	 * 回传数据
	 */
	String callbackData;

	public ChatRoomCreateMsgFilterFinishMsg() {
		super(MsgId.CREATE_CHATROOM_MSG_FILTER);
	}
	
	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static ChatRoomCreateMsgFilterFinishMsg valueOf(String msgContent, String callbackData) {
		ChatRoomCreateMsgFilterFinishMsg msg = HawkObjectPool.getInstance().borrowObject(ChatRoomCreateMsgFilterFinishMsg.class);
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
