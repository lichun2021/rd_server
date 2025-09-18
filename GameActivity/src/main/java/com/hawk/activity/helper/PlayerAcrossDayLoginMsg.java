package com.hawk.activity.helper;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

public class PlayerAcrossDayLoginMsg extends HawkMsg {
	
	private boolean isLogin;
	
	public PlayerAcrossDayLoginMsg() {
		super(MsgId.PLAYER_ACROSS_DAY_LOGIN);
	}

	public static PlayerAcrossDayLoginMsg valueOf(boolean isLogin) {
		PlayerAcrossDayLoginMsg msg = new PlayerAcrossDayLoginMsg();
		msg.isLogin = isLogin;
		return msg;
	}

	public boolean isLogin() {
		return isLogin;
	}
}
