package com.hawk.game.lianmengstarwars;

import java.util.concurrent.TimeUnit;

public class SWConst {
	/** 一分钟毫秒数 */
	public static final long MINUTE_MICROS = TimeUnit.MINUTES.toMillis(1);

	public static class ModuleType {
		public static final int SWWorld = 3110;
		public static final int SWMarch = 3111;
		public static final int SWArmy = 3112;
		public static final int SWGuildFormation = 3113;
	}
	
	public enum SWOverType{
		/**连续控制1/4时间*/
		CTCROL, 
		/**累计控制1/2*/
		LJCROL,
		/**时间结束*/
		TIMEOVER;
	}

	public enum SWState {
		/** 联盟军演战场中 */
		GAMEING(5);

		SWState(int value) {
			this.value = value;
		}

		private int value;

		public int intValue() {
			return value;
		}
	}
}
