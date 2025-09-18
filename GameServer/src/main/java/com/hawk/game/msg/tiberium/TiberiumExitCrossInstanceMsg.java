package com.hawk.game.msg.tiberium;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst;

public class TiberiumExitCrossInstanceMsg extends HawkMsg {
	public TiberiumExitCrossInstanceMsg() {
		super(GameConst.MsgId.TIBERIUM_EXIT_INSTANCE);
	}
}
