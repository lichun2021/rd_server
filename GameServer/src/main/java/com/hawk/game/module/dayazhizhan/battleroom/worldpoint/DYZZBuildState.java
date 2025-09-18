package com.hawk.game.module.dayazhizhan.battleroom.worldpoint;

public enum DYZZBuildState {
	/**中立 */
	ZHONG_LI(0),
	/**占领中*/
	ZHAN_LING_ZHONG(1),
	/**已占*/
	ZHAN_LING(2),
	/**摧毁中 只有箭塔可摧毁*/
	CUI_HUI(3),
	/**能源井冷却中*/
	PROTECTED(4);

	DYZZBuildState(int value) {
		this.value = value;
	}

	private int value;

	public int intValue() {
		return value;
	}
}
