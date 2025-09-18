package com.hawk.game.lianmengcyb;

import java.util.concurrent.TimeUnit;

public class CYBORGConst {
	/** 一分钟毫秒数 */
	public static final long MINUTE_MICROS = TimeUnit.MINUTES.toMillis(1);

	public static class ModuleType {
		public static final int CYBORGWorld = 4110;
		public static final int CYBORGMarch = 4111;
		public static final int CYBORGArmy = 4112;
	}

	public enum CYBORGState {
		/** 联盟军演战场中 */
		GAMEING(7);

		CYBORGState(int value) {
			this.value = value;
		}

		private int value;

		public int intValue() {
			return value;
		}
	}
}
