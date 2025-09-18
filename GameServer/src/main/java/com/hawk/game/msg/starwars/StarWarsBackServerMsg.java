package com.hawk.game.msg.starwars;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst;

public class StarWarsBackServerMsg extends HawkMsg {
	public StarWarsBackServerMsg() {
		super(GameConst.MsgId.STAR_WARS_BACK_SERVER);
	}
}
