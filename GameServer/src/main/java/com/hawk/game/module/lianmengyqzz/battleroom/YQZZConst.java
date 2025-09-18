package com.hawk.game.module.lianmengyqzz.battleroom;

import java.util.concurrent.TimeUnit;

public class YQZZConst {
	/** 一分钟毫秒数 */
	public static final long MINUTE_MICROS = TimeUnit.MINUTES.toMillis(1);
	public static final String YURI_GUILD = "YURI";
	
	public static class ModuleType {
		public static final int YQZZWorld = 3330;
		public static final int YQZZMarch = 3331;
		public static final int YQZZArmy = 3332;
		public static final int YQZZIdle = 3333;
		public static final int YQZZGuildFormation = 3334;
	}

	// public enum YQZZOverType {
	// /**连续控制1/4时间*/
	// CTCROL,
	// /**累计控制1/2*/
	// LJCROL,
	// /**时间结束*/
	// TIMEOVER;
	// }

	public enum YQZZState {
		/** 联盟军演战场中 */
		GAMEING(11);

		YQZZState(int value) {
			this.value = value;
		}

		private int value;

		public int intValue() {
			return value;
		}
	}
}
