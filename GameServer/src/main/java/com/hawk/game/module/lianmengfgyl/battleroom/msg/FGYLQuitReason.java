package com.hawk.game.module.lianmengfgyl.battleroom.msg;

public enum FGYLQuitReason {
	/** 主动退出 */
	LEAVE(1), GAMEOVER(0);

	private int value;

	FGYLQuitReason(int value) {
		this.value = value;
	}

	public int intValue() {
		return value;
	}
}