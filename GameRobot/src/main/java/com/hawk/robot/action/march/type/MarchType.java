package com.hawk.robot.action.march.type;

import java.util.Arrays;
import java.util.Optional;

public enum MarchType implements IntConst {

	/**
	 * 攻击玩家
	 */
	ATTACK_PLAYER(1),
	
	/**
	 * 攻击野怪
	 */
	ATTACK_MONSTER(2),
	
	/**
	 * 侦查
	 */
	INVESTIGATION(3),

	/**
	 * 采集
	 */
	COLLECT_RESOUREC(4),
	
	/**
	 * 驻扎
	 */
	QUARTERED(5),
	
	/**
	 * 集结进攻玩家基地
	 */
	MASS(6),
	
	/**
	 * 参与集结进攻玩家基地
	 */
	MASS_JOIN(7),
	
	/**
	 * 集结进攻野怪
	 */
	MASS_MONSTER(8),
	
	/**
	 * 参与集结进攻野怪
	 */
	MASS_MONSTER_JOIN(9),
	
	/**
	 * 尤里探索
	 */
	YURI_EXPLORE(10),
	
	/**
	 * 随机宝箱
	 */
	RANDOM_BOX(11),
	
	/**
	 * 攻击联盟领地
	 */
	ATTACK_MANOR(12),
	
	/**
	 * 集结攻击联盟领地
	 */
	MASS_MANOR(13),
	
	/**
	 * 加入集结攻击联盟领地
	 */
	MASS_MANOR_JOIN(14),
	
	/**
	 * 加入集结攻击联盟领地
	 */
	STRONGPOINT(15),
	
	/**
	 * 攻击新版野怪
	 */
	ATTACK_NEW_MONSTER(16),
	
	/**
	 * 集结迷雾要塞
	 */
	MASS_FOGGY(17),
	
	/**
	 * 集结加入迷雾要塞
	 */
	MASS_JOIN_FOGGY(18),
	;
	
	
	private final int value;
	
	
	private MarchType(int value) {
		this.value = value;
	}
	
	public int intVal() {
		return value;
	}
	
	public static boolean isSimpleMarchType(MarchType marchType) {
		if (marchType == MarchType.COLLECT_RESOUREC || marchType == MarchType.ATTACK_PLAYER 
				|| marchType == MarchType.ATTACK_MONSTER || marchType == MarchType.INVESTIGATION 
				|| marchType == MarchType.QUARTERED || marchType == MarchType.YURI_EXPLORE
				|| marchType == MarchType.RANDOM_BOX || marchType == MarchType.STRONGPOINT 
				|| marchType == MarchType.ATTACK_NEW_MONSTER) {
			return true;
		}
		return false;
	}
	
	public static MarchType valueOf(int value) {
	     return valueOfEnum(MarchType.values(), value);
	}
	
	private static <T extends IntConst> T valueOfEnum(T[] values, int value) {
		Optional<T> op = Arrays.stream(values).filter(o -> o.intVal() == value).findAny();
		if (op.isPresent()) {
			return op.get();
		}
		throw new RuntimeException("incorrect enum value : " + value);
	}
}

interface IntConst {
	int intVal();
}
