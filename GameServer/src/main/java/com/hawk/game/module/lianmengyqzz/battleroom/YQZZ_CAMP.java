package com.hawk.game.module.lianmengyqzz.battleroom;

/** 红 蓝方 */
public enum YQZZ_CAMP {
	A(1), B(2), C(3), D(4), E(5), F(6), FOGGY(107);
	YQZZ_CAMP(int value) {
		this.value = value;
	}
 
	private int value;

	public int intValue() {
		return value;
	}

	public static YQZZ_CAMP valueOf(int v) {
		switch (v) {
		case 1:
			return A;
		case 2:
			return B;

		case 3:
			return C;

		case 4:
			return D;
		case 5:
			return E;
		case 6:
			return F;
		case 107:
			return FOGGY;

		default:
			throw new RuntimeException("no such camp = " + v);
		}
	}
}