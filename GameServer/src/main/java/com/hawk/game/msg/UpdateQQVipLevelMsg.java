package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 更新QQ vip等级信息
 */
public class UpdateQQVipLevelMsg extends HawkMsg {
	
	private int level;

	public UpdateQQVipLevelMsg() {
		super(MsgId.UPDATE_QQVIP_LEVEL);
	}

	public static UpdateQQVipLevelMsg valueOf(int level) {
		UpdateQQVipLevelMsg msg = new UpdateQQVipLevelMsg();
		msg.level = level;
		return msg;
	}

	public int getLevel() {
		return level;
	}

}
