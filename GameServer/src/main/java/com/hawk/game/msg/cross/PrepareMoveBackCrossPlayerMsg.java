package com.hawk.game.msg.cross;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst;

/**
 * gm后台发起的强制签回玩家.
 * @author jm
 *
 */
public class PrepareMoveBackCrossPlayerMsg extends HawkMsg {
	/**
	 * 是否是强制签回玩家.
	 */
	private boolean force;
	public PrepareMoveBackCrossPlayerMsg(boolean force) {
		super(GameConst.MsgId.CROSS_PREPARE_FORCE_MOVE_BACK);
		this.force = force;
	}
	public boolean isIsforce() {
		return this.force;
	}
	
	
}
