package com.hawk.game.player.laboratory;

public class LaboratoryEnum {
	private LaboratoryEnum() {
	}

	/**魔方卡槽位*/
	public enum PowerBlockIndex {
		ONE(1), TWO(2), THREE(3), FOUR(4);
		PowerBlockIndex(int value) {
			this.INT_VAL = value;
		}

		public final int INT_VAL;

		public static PowerBlockIndex valueOf(int val) {
			switch (val) {
			case 1:
				return ONE;
			case 2:
				return TWO;
			case 3:
				return THREE;
			case 4:
				return FOUR;
			default:
				break;
			}
			return null;
		}
	}

	/** 能量核心类型*/
	public enum PowerCoreIndex {
		ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6);
		PowerCoreIndex(int value) {
			this.INT_VAL = value;
		}

		public final int INT_VAL;

		public static PowerCoreIndex valueOf(int val) {
			switch (val) {
			case 1:
				return ONE;
			case 2:
				return TWO;
			case 3:
				return THREE;
			case 4:
				return FOUR;
			case 5:
				return FIVE;
			case 6:
				return SIX;
			default:
				break;
			}
			return null;
		}

	}

}
