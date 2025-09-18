package com.hawk.game.module.lianmengfgyl.battleroom.worldpoint;

public enum FGYLBuildType {
	YI(1), ER(2), SAN(3), SI(4), WU(5), LIU(6), QI(7), BA(8), JIU(9), SHI(10), SHIYI(11), SHIER(12), SHISAN(13), SHISI(14), SHIWU(15), SHILIU(16), SHIQI(17), SHIBA(18), SHIJIU(
			19), ERSHI(20);
	FGYLBuildType(int value) {
		this.value = value;
	}

	private int value;

	public int intValue() {
		return value;
	}

	public static FGYLBuildType valueOf(int v) {
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
		case 13:
			return SHISAN;
		case 14:
			return SHISI;
		case 15:
			return SHIWU;
		case 16:
			return SHILIU;
		case 17:
			return SHIQI;
		case 18:
			return SHIBA;
		case 19:
			return SHIJIU;
		case 20:
			return ERSHI;

		default:
			throw new RuntimeException("no such camp = " + v);
		}
	}
}
