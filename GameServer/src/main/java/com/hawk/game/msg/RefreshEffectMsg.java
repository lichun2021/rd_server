package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 刷新作用号
 * @author jm
 *
 */
public class RefreshEffectMsg extends HawkMsg {
	/**
	 * 刷新effectI
	 */
	private int effectId;

	public int getEffectId() {
		return effectId;
	}

	public void setEffectId(int effectId) {
		this.effectId = effectId;
	}
	
	
}
