package com.hawk.game.msg.starwars;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst;

public class StarWarsPrepareExitCrossInstanceMsg extends HawkMsg {
	public StarWarsPrepareExitCrossInstanceMsg() {
		super(GameConst.MsgId.STAR_WARS_PREPARE_EXIT_CROSS_INSTANCE);
	}
}
