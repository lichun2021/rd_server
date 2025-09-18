package com.hawk.game.module.lianmengyqzz.battleroom.worldpoint;

public enum YQZZBuildState {
	/**中立*/
	ZHONG_LI(0),
	/**占领中*/
	ZHAN_LING_ZHONG(1),
	/**已占*/
	ZHAN_LING(2);
	YQZZBuildState(int value) {
		this.value = value;
	}

	private int value;

	public int intValue() {
		return value;
	}
}
