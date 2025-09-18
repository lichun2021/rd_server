package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.queryentity.SuperArmourInfo;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 删除铠甲
 * @author golden
 *
 */
public class RemoveArmourMsg extends HawkMsg {

	public SuperArmourInfo info;
	
	public RemoveArmourMsg(SuperArmourInfo info) {
		super(MsgId.REMOVE_ARMOUR);
		this.info = info;
	}

	public SuperArmourInfo getInfo() {
		return info;
	}

	public void setInfo(SuperArmourInfo info) {
		this.info = info;
	}
	
}
