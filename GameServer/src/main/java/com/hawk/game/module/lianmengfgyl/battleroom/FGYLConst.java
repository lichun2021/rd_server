package com.hawk.game.module.lianmengfgyl.battleroom;

import java.util.concurrent.TimeUnit;

public class FGYLConst {
	/** 一分钟毫秒数 */
	public static final long MINUTE_MICROS = TimeUnit.MINUTES.toMillis(1);

	public static class ModuleType {
		public static final int FGYLWorld = 2120;
		public static final int FGYLMarch = 2121;
		public static final int FGYLArmy = 2122;
		public static final int FGYLGuildFormation = 2123;
	}

	public enum FGYLState {
		/** 联盟军演战场中 */
		GAMEING(15);

		FGYLState(int value) {
			this.value = value;
		}

		private int value;

		public int intValue() {
			return value;
		}
	}
}
