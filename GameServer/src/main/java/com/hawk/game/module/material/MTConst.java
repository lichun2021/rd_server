package com.hawk.game.module.material;

public interface MTConst {
	public enum MTTruckType {
		SINGLE(1), // 预备
		GUILD(2), // 发出
		GUILDBIG(3); // 到啦
		int index;

		private MTTruckType(int index) {
			this.index = index;
		}

		public int getNumber() {
			return index;
		}

		public static MTTruckType valueOf(int index) {
			switch (index) {
			case 1:
				return SINGLE;
			case 2:
				return GUILD;
			case 3:
				return GUILDBIG;
			default:
				break;
			}
			return null;
		}
	}
	
	public enum MTMarchIndex {
		YACHE(998), // 跟车行军
		LUEDUO(688); // 掠夺行军

		int index;

		private MTMarchIndex(int index) {
			this.index = index;
		}

		public int getNumber() {
			return index;
		}

		public static MTMarchIndex valueOf(int index) {
			switch (index) {
			case 998:
				return YACHE;
			case 688:
				return LUEDUO;
			default:
				break;
			}
			return null;
		}
	}

	public enum MTTruckState {
		PRE(1), // 预备
		MARCH(2), // 发出
		REACHED(3); // 到啦
		int index;

		private MTTruckState(int index) {
			this.index = index;
		}

		public int getNumber() {
			return index;
		}

		public static MTTruckState valueOf(int index) {
			switch (index) {
			case 1:
				return PRE;
			case 2:
				return MARCH;
			case 3:
				return REACHED;
			default:
				break;
			}
			return null;
		}
	}
}
