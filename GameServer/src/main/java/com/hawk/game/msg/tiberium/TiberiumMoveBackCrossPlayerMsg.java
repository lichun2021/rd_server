package com.hawk.game.msg.tiberium;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst;

public class TiberiumMoveBackCrossPlayerMsg extends HawkMsg {
	public TiberiumMoveBackCrossPlayerMsg() {
		super(GameConst.MsgId.TIBERIUM_MOVE_BACK);
	}
}
