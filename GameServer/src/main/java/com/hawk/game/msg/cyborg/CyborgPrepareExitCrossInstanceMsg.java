package com.hawk.game.msg.cyborg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst;

public class CyborgPrepareExitCrossInstanceMsg extends HawkMsg {
	public CyborgPrepareExitCrossInstanceMsg() {
		super(GameConst.MsgId.CYBORG_PREPARE_EXIT_CROSS_INSTANCE);
	}
}
