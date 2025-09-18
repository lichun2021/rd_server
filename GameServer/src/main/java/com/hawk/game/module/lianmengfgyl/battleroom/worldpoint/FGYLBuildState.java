package com.hawk.game.module.lianmengfgyl.battleroom.worldpoint;

public enum FGYLBuildState {
	/**幽灵占领*/
	YOULING(1),
	/**已占*/
	ZHAN_LING(2);

	FGYLBuildState(int value) {
		this.value = value;
	}

	private int value;

	public int intValue() {
		return value;
	}
}
