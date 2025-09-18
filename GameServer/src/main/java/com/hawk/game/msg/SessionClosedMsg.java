package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 会话关闭
 * 
 * @author hawk
 *
 */
public class SessionClosedMsg extends HawkMsg {
	/**
	 * 构造
	 */
	public SessionClosedMsg() {
		super(MsgId.SESSION_CLOSE);
	}

	/**
	 * 构建消息对象
	 * 
	 * @return
	 */
	public static SessionClosedMsg valueOf() {
		return new SessionClosedMsg();
	}
}
