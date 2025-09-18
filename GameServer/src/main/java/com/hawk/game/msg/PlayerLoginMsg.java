package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.net.session.HawkSession;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 玩家登陆
 * 
 * @author hawk
 *
 */
public class PlayerLoginMsg extends HawkMsg {
	/**
	 * 会话对象
	 */
	private HawkSession session;

	public HawkSession getSession() {
		return session;
	}

	public void setSession(HawkSession session) {
		this.session = session;
	}
	
	public PlayerLoginMsg() {
		super(MsgId.PLAYER_LOGIN);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static PlayerLoginMsg valueOf(HawkSession session) {
		PlayerLoginMsg msg = new PlayerLoginMsg();
		msg.session = session;
		return msg;
	}
}
