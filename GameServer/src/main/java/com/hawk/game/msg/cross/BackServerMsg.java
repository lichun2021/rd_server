package com.hawk.game.msg.cross;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst;

/**
 * A->B
 *A 处理该消息.
 * @author jm
 *
 */
public class BackServerMsg extends HawkMsg {
	public BackServerMsg() {
		super(GameConst.MsgId.CROSS_BACK_SERVER);
	}
}
