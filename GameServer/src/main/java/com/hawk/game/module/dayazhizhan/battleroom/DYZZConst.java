package com.hawk.game.module.dayazhizhan.battleroom;

import java.util.concurrent.TimeUnit;

public class DYZZConst {
	/** 一分钟毫秒数 */
	public static final long MINUTE_MICROS = TimeUnit.MINUTES.toMillis(1);

	public static class ModuleType {
		public static final int DYZZWorld = 5110;
		public static final int DYZZMarch = 5111;
		public static final int DYZZArmy = 5112;
	}

	public enum DYZZShotType {
		Turn(1), EnergyWell(2), Order(3);
		DYZZShotType(int value) {
			this.value = value;
		}

		private int value;

		public int intValue() {
			return value;
		}
	}

	public enum DYZZState {
		/** 联盟军演战场中 */
		GAMEING(9);

		DYZZState(int value) {
			this.value = value;
		}

		private int value;

		public int intValue() {
			return value;
		}
	}
}
