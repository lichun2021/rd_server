package com.hawk.game.heroTrial;

public enum HeroTrialType {

	// 军事属性总和达到A
	TYPE_1(1),

	// 工业属性总和达到A
	TYPE_2(2),
	
	// 后勤属性总和达到A
	TYPE_3(3),
	
	// A等级B品质C名
	TYPE_4(4),
	
	// 有A等级B星级C英雄
	TYPE_5(5),
	
	// 携带A等级B芯片C个
	TYPE_6(6),
	
	// 有A条天赋
	TYPE_7(7),
	;
	
	private int type;
	
	private HeroTrialType(int type) {
		this.type = type;
	}
	
	public int intValue() {
		return type;
	}
	
	/**
	 * 任务类型  MissionType
	 * 
	 * @param type
	 * @return
	 */
	public static HeroTrialType valueOf(int type) {
		for (HeroTrialType HTType : values()) {
			if (HTType.intValue() == type) {
				return HTType;
			}
		}
		return null;
	}
}
