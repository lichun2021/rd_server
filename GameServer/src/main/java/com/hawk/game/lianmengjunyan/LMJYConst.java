package com.hawk.game.lianmengjunyan;

public class LMJYConst {
	public static class ModuleType {
		public static final int HLGWorld = 1110;
		public static final int HLGMarch = 1111;
		public static final int HLGArmy = 1112;
	}

	public enum PState {
		/** 可以进入 */
		PREJOIN(1),
		/** 战场中 */
		GAMEING(2);

		PState(int value) {
			this.value = value;
		}

		private int value;

		public int intValue() {
			return value;
		}
	}
}
