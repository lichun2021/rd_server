package com.hawk.game.msg.cyborg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst;

public class CyborgPrepareMoveBackMsg extends HawkMsg {
	public CyborgPrepareMoveBackMsg() {
		super(GameConst.MsgId.CYBORG_PREPARE_MOVE_BACK);
	}
}
