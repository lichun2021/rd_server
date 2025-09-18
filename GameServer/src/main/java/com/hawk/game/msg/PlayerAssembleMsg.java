package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.net.session.HawkSession;

import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 玩家组装
 * 
 * @author hawk
 *
 */
public class PlayerAssembleMsg extends HawkMsg {
	/**
	 * 登陆协议对象
	 */
	private HPLogin loginCmd;
	/**
	 * 会话对象
	 */
	private HawkSession session;
	
	public HPLogin getLoginCmd() {
		return loginCmd;
	}

	public void setLoginCmd(HPLogin loginCmd) {
		this.loginCmd = loginCmd;
	}

	public HawkSession getSession() {
		return session;
	}

	public void setSession(HawkSession session) {
		this.session = session;
	}
	
	public PlayerAssembleMsg() {
		super(MsgId.PLAYER_ASSEMBLE);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static PlayerAssembleMsg valueOf(HPLogin loginCmd, HawkSession session) {
		PlayerAssembleMsg msg = new PlayerAssembleMsg();
		msg.loginCmd = loginCmd;
		msg.session = session;
		return msg;
	}
}
