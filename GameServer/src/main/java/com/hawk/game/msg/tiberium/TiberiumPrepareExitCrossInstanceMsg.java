package com.hawk.game.msg.tiberium;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst;

public class TiberiumPrepareExitCrossInstanceMsg extends HawkMsg {
	public TiberiumPrepareExitCrossInstanceMsg() {
		super(GameConst.MsgId.TIBERIUM_PREPARE_EXIT_CROSS_INSTANCE);
	}
}
