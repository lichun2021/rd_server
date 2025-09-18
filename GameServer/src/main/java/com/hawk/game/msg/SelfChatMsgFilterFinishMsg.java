package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.pool.HawkObjectPool;
import org.hawk.pool.ObjectPool;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 禁言中自发信息过滤完成消息
 * 
 * @author lating
 *
 */
@ObjectPool.Declare(minIdle = 128, maxIdle = 2048, timeBetweenEvictionRunsMillis = 300000, minEvictableIdleTimeMillis = 600000)
public class SelfChatMsgFilterFinishMsg extends HawkMsg {
	/**
	 * 过滤后的消息内容
	 */
	String msgContent;
	/**
	 * 聊天类型
	 */
	int chatType;

	public SelfChatMsgFilterFinishMsg() {
		super(MsgId.SELF_CHAT_MSG_FILTER);
	}
	
	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static SelfChatMsgFilterFinishMsg valueOf(String msgContent, String chatType) {
		SelfChatMsgFilterFinishMsg msg = HawkObjectPool.getInstance().borrowObject(SelfChatMsgFilterFinishMsg.class);
		msg.msgContent = msgContent;
		msg.chatType = Integer.valueOf(chatType);
		return msg;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public int getChatType() {
		return chatType;
	}

	public void setChatType(int chatType) {
		this.chatType = chatType;
	}
	
}
