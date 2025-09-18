package com.hawk.game.module.lianmengtaiboliya;

import java.util.concurrent.TimeUnit;

public class TBLYConst {
	/** 一分钟毫秒数 */
	public static final long MINUTE_MICROS = TimeUnit.MINUTES.toMillis(1);

	public static class ModuleType {
		public static final int TBLYWorld = 2110;
		public static final int TBLYMarch = 2111;
		public static final int TBLYArmy = 2112;
		public static final int TBLYGuildFormation = 2113;
	}

	public enum TBLYState {
		/** 联盟军演战场中 */
		GAMEING(3);

		TBLYState(int value) {
			this.value = value;
		}

		private int value;

		public int intValue() {
			return value;
		}
	}
}
