package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 装备研究升级消息
 * @author Golden
 *
 */
public class EquipResearchLevelUpMsg extends HawkMsg {

	/**
	 * 装备研究id
	 */
	private int researchId;
	
	/**
	 * 装备研究等级
	 */
	private int researchLevel;
	
	/**
	 * 构造消息对象
	 */
	public static EquipResearchLevelUpMsg valueOf(int researchId, int researchLevel) {
		EquipResearchLevelUpMsg msg = new EquipResearchLevelUpMsg();
		msg.researchId = researchId;
		msg.researchLevel = researchLevel;
		return msg;
	}

	public int getResearchId() {
		return researchId;
	}

	public int getResearchLevel() {
		return researchLevel;
	}
}