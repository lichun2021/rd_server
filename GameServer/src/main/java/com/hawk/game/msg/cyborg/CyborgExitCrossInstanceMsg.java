package com.hawk.game.msg.cyborg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst;

public class CyborgExitCrossInstanceMsg extends HawkMsg {
	public CyborgExitCrossInstanceMsg() {
		super(GameConst.MsgId.CYBORG_EXIT_INSTANCE);
	}
}
