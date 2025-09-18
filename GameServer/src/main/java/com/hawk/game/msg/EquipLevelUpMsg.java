package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 装备升级消息
 * @author Golden
 *
 */
public class EquipLevelUpMsg extends HawkMsg {
	
	/**
	 * 升级后等级
	 */
	private int afterLevel;
	
	public static EquipLevelUpMsg valueOf(int afterLevel) {
		EquipLevelUpMsg msg = new EquipLevelUpMsg();
		msg.afterLevel = afterLevel;
		return msg;
	}

	public int getAfterLevel() {
		return afterLevel;
	}
}
