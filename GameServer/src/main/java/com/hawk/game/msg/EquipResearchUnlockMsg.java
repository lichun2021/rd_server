package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 装备科技解锁
 * @author Golden
 *
 */
public class EquipResearchUnlockMsg extends HawkMsg {
	
	public static EquipResearchUnlockMsg valueOf() {
		EquipResearchUnlockMsg msg = new EquipResearchUnlockMsg();
		return msg;
	}
}
