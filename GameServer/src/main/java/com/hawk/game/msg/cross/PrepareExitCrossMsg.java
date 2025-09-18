package com.hawk.game.msg.cross;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 从A服跨到B服,
 * 离开B服,B服处理此消息.
 * 活动那边发起玩家退出协议.
 * @author jm
 *
 */
public class PrepareExitCrossMsg extends HawkMsg {
	public PrepareExitCrossMsg() {
		super(MsgId.CORSS_PREPARE_EXIT);
	}
}
