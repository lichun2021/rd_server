package com.hawk.game.module.lianmengyqzz.battleroom.worldpoint;

public enum YQZZBuildType {
	YI(1), ER(2), SAN(3), SI(4), WU(5), LIU(6), QI(7), BA(8), JIU(9), SHI(10), SHIYI(11), SHIER(12);
	YQZZBuildType(int value) {
		this.value = value;
	}

	private int value;

	public int intValue() {
		return value;
	}

	public static YQZZBuildType valueOf(int v) {
		switch (v) {
		case 1:
			return YI;
		case 2:
			return ER;

		case 3:
			return SAN;

		case 4:
			return SI;
		case 5:
			return WU;
		case 6:
			return LIU;
		case 7:
			return QI;
		case 8:
			return BA;
		case 9:
			return JIU;
		case 10:
			return SHI;
		case 11:
			return SHIYI;
		case 12:
			return SHIER;

		default:
			throw new RuntimeException("no such camp = " + v);
		}
	}
}
