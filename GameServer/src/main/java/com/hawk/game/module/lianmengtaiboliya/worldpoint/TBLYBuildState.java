package com.hawk.game.module.lianmengtaiboliya.worldpoint;

public enum TBLYBuildState {
	/**中立*/
	ZHONG_LI(0),
	/**占领中*/
	ZHAN_LING_ZHONG(1),
	/**已占*/
	ZHAN_LING(2);

	TBLYBuildState(int value) {
		this.value = value;
	}

	private int value;

	public int intValue() {
		return value;
	}
}
