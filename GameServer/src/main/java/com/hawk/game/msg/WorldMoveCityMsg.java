package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 玩家迁城
 */
public class WorldMoveCityMsg extends HawkMsg {

	public WorldMoveCityMsg() {
		super(MsgId.WORLD_MOVE_CITY);
	}

	public static WorldMoveCityMsg valueOf() {
		WorldMoveCityMsg msg = new WorldMoveCityMsg();
		return msg;
	}
}
