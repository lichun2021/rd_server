package com.hawk.game.msg.tiberium;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst;

public class TiberiumPrepareMoveBackMsg extends HawkMsg {
	public TiberiumPrepareMoveBackMsg() {
		super(GameConst.MsgId.TIBERIUM_PREPARE_MOVE_BACK);
	}
}
