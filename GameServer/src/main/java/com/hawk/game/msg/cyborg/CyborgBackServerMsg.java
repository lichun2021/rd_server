package com.hawk.game.msg.cyborg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst;

public class CyborgBackServerMsg extends HawkMsg {
	public CyborgBackServerMsg() {
		super(GameConst.MsgId.CYBORG_BACK_SERVER);
	}
}
