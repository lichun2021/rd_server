package com.hawk.game.msg.starwars;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst;

public class StarWarsExitCrossInstanceMsg extends HawkMsg {
	public StarWarsExitCrossInstanceMsg() {
		super(GameConst.MsgId.STAR_WARS_EXIT_INSTANCE);
	}
}
