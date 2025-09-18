package com.hawk.game.module.lianmenxhjz.battleroom.msg;

public enum XHJZQuitReason {
	/** 主动退出 */
	LEAVE(1), GAMEOVER(0);

	private int value;

	XHJZQuitReason(int value) {
		this.value = value;
	}

	public int intValue() {
		return value;
	}
}