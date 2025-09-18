package com.hawk.game.lianmengstarwars.worldpoint;

public enum SWBuildState {
	/**中立*/
	ZHONG_LI(0),
	/**占领中*/
	ZHAN_LING_ZHONG(1),
	/**已占*/
	ZHAN_LING(2);

	SWBuildState(int value) {
		this.value = value;
	}

	private int value;

	public int intValue() {
		return value;
	}
}
