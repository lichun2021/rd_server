package com.hawk.game.msg.starwars;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst;

public class StarWarsMoveBackCrossPlayerMsg extends HawkMsg {
	public StarWarsMoveBackCrossPlayerMsg() {
		super(GameConst.MsgId.STAR_WARS_MOVE_BACK);
	}
}
