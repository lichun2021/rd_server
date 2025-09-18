package com.hawk.game.module.lianmengXianquhx;

import java.util.concurrent.TimeUnit;

public class XQHXConst {
	/** 一分钟毫秒数 */
	public static final long MINUTE_MICROS = TimeUnit.MINUTES.toMillis(1);

	public static class ModuleType {
		public static final int XQHXWorld = 2110;
		public static final int XQHXMarch = 2111;
		public static final int XQHXArmy = 2112;
		public static final int XQHXGuildFormation = 2113;
	}

	/**	- buildTypeId=1，核心建筑*/
	public static int BuildType1 = 1;//
	/**	- buildTypeId=2，次核心建筑 */
	public static int BuildType2 = 2;//
	/**	- buildTypeId=3，次要建筑*/
	public static int BuildType3 = 3;//

	public enum XQHXState {
		/** 联盟军演战场中 */
		GAMEING(17);

		XQHXState(int value) {
			this.value = value;
		}

		private int value;

		public int intValue() {
			return value;
		}
	}
}
