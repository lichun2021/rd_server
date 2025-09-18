package com.hawk.game.service.mail;

public enum DungeonMailType {
	NONE(0), LMJY(1), TBLY(3), SW(5), CYBORG(7), DYZZ(9), YQZZ(11), XHJZ(13), FGYL(15), XQHX(17), TOUCAI(101);

	DungeonMailType(int value) {
		this.value = value;
	}

	private int value;

	public int intValue() {
		return value;
	}

	public static DungeonMailType valueOf(int duntype) {
		switch (duntype) {
		case 0:
			return NONE;
		case 1:
			return LMJY;

		case 3:
			return TBLY;

		case 5:
			return SW;

		case 7:
			return CYBORG;
		case 9:
			return DYZZ;
		case 11:
			return YQZZ;
		case 13:
			return XHJZ;
		case 15:
			return FGYL;
		case 17:
			return XQHX;
		case 101:
			return TOUCAI;
		default:
			break;
		}
		return null;
	}
}
