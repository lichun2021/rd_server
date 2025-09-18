package com.hawk.game.module.lianmenxhjz.battleroom.worldpoint;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum XHJZBuildType {
	YI(1), ER(2), SAN(3), SI(4), WU(5), LIU(6), QI(7), BA(8), JIU(9), SHI(10), SHIYI(11), SHIER(12), SHISAN(13), SHISI(14), SHIWU(15), SHILIU(16), SHIQI(17), SHIBA(18), SHIJIU(
			19), ERSHI(20), XXI(21), XXII(22), XXIII(23), XXIV(24), XXV(25), XXVI(
					26), XXVII(27), XXVIII(28), XXIX(29), XXX(30), XXXI(31), XXXII(32), XXXIII(33), XXXIV(34), XXXV(35), XXXVI(36), XXXVII(37), XXXVIII(38), XXXIX(39), XL(40);

	static Map<Integer, XHJZBuildType> valueMap;

	XHJZBuildType(int value) {
		this.value = value;
	}

	private int value;

	public int intValue() {
		return value;
	}

	public static XHJZBuildType valueOf(int v) {
		if (valueMap == null) {
			valueMap = Stream.of(XHJZBuildType.values()).collect(Collectors.toMap(a -> a.intValue(), a -> a));
		}

		return valueMap.get(v);
	}
}
