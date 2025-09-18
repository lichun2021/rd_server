package com.hawk.game.module.lianmenxhjz.battleroom;

import java.util.concurrent.TimeUnit;

public class XHJZConst {
	/** 一分钟毫秒数 */
	public static final long MINUTE_MICROS = TimeUnit.MINUTES.toMillis(1);

	public static class ModuleType {
		public static final int XHJZWorld = 2120;
		public static final int XHJZMarch = 2121;
		public static final int XHJZArmy = 2122;
		public static final int XHJZGuildFormation = 2123;
	}

	public enum XHJZState {
		/** 联盟军演战场中 */
		GAMEING(13);

		XHJZState(int value) {
			this.value = value;
		}

		private int value;

		public int intValue() {
			return value;
		}
	}
}
