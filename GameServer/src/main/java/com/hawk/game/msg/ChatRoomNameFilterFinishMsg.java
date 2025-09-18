package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.pool.HawkObjectPool;
import org.hawk.pool.ObjectPool;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 聊天室名称过滤完成消息
 * 
 * @author lating
 *
 */
@ObjectPool.Declare(minIdle = 128, maxIdle = 2048, timeBetweenEvictionRunsMillis = 300000, minEvictableIdleTimeMillis = 600000)
public class ChatRoomNameFilterFinishMsg extends HawkMsg {
	/**
	 * 过滤后的消息内容
	 */
	String msgContent;
	/**
	 * 被传回的数据
	 */
	String callbackData;
	
	int resultCode;

	public ChatRoomNameFilterFinishMsg() {
		super(MsgId.CHATROOM_NAME_FILTER);
	}
	
	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static ChatRoomNameFilterFinishMsg valueOf(int resultCode, String msgContent, String callbackData) {
		ChatRoomNameFilterFinishMsg msg = HawkObjectPool.getInstance().borrowObject(ChatRoomNameFilterFinishMsg.class);
		msg.msgContent = msgContent;
		msg.callbackData = callbackData;
		msg.resultCode = resultCode;
		return msg;
	}

	public int getResultCode() {
		return resultCode;
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
