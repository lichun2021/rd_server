package com.hawk.game.msg.starwars;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst;

public class StarWarsPrepareMoveBackMsg extends HawkMsg {
	public StarWarsPrepareMoveBackMsg() {
		super(GameConst.MsgId.STAR_WARS_PREPARE_MOVE_BACK);
	}
}
