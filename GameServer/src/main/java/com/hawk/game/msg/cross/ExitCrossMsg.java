package com.hawk.game.msg.cross;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 从A服跨到B服,
 * 离开B服,B服处理此消息.
 * 退出的最后一步,实际退出.
 * @author jm
 *
 */
public class ExitCrossMsg extends HawkMsg {
	public ExitCrossMsg() {
		super(MsgId.CROSS_EIXT);
	}
}
