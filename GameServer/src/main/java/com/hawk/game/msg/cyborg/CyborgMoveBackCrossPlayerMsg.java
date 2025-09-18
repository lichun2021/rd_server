package com.hawk.game.msg.cyborg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst;

public class CyborgMoveBackCrossPlayerMsg extends HawkMsg {
	public CyborgMoveBackCrossPlayerMsg() {
		super(GameConst.MsgId.CYBORG_MOVE_BACK);
	}
}
