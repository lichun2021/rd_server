package com.hawk.game.battle.effect;

import java.util.Map;

import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

public interface IChecker {

	/**
	 * 是否对不同目标类型敏感. 如对防御坦克增加额外伤害 true. 自身攻击增加xx false
	 * @return
	 */
	default boolean tarTypeSensitive(){
		return true;
	}
	
	/**
	 * 配置ID
	 */
	default EffType effType() {
		return getClass().getAnnotation(EffectChecker.class).effType();
	}

	/**
	 * 该兵种是否是陷阱
	 * @param soldierType
	 * @return
	 */
	default boolean isWeapon(SoldierType soldierType) {
		return soldierType == SoldierType.WEAPON_LANDMINE_101 || soldierType == SoldierType.WEAPON_ACKACK_102
				|| soldierType == SoldierType.WEAPON_ANTI_TANK_103;
	}
	
	default boolean isPlan(SoldierType soldierType) {
		return soldierType == SoldierType.PLANE_SOLDIER_3 || soldierType == SoldierType.PLANE_SOLDIER_4;
	}
	
	default boolean isTank(SoldierType soldierType) {
		return soldierType == SoldierType.TANK_SOLDIER_1 || soldierType == SoldierType.TANK_SOLDIER_2;
	}
	
	default boolean isFoot(SoldierType soldierType) {
		return soldierType == SoldierType.FOOT_SOLDIER_5 || soldierType == SoldierType.FOOT_SOLDIER_6;
	}

	/**
	 * 该兵种是否是普通兵种
	 * @param soldierType
	 * @return
	 */
	default boolean isSoldier(SoldierType soldierType) {
		return soldierType.getNumber() <= 8;
	}

	default boolean isJinzhan(SoldierType type) {
		if (type == SoldierType.TANK_SOLDIER_1
				|| type == SoldierType.TANK_SOLDIER_2
				|| type == SoldierType.PLANE_SOLDIER_3
				|| type == SoldierType.CANNON_SOLDIER_8) {
			return true;
		}
		return false;
	}
	
	/** 是远程单位*/
	default boolean isYuanCheng(SoldierType type) {
		if (type == SoldierType.PLANE_SOLDIER_4
				|| type == SoldierType.FOOT_SOLDIER_5
				|| type == SoldierType.FOOT_SOLDIER_6
				|| type == SoldierType.CANNON_SOLDIER_7) {
			return true;
		}
		return false;
	}

	CheckerKVResult value(CheckerParames parames);
	
	default CheckerKVResult valueV2(CheckerParames parames) {
		if (tarTypeSensitive()) {
			return value(parames);
		}

		Map<EffType, CheckerKVResult> cache = parames.unity.getTarTypeNotSensitiveCache();
		if (cache.containsKey(effType())) {
			return cache.get(effType());
		}

		CheckerKVResult result = value(parames);
		cache.put(effType(), result);
		return result;
	}

	default String getSimpleName() {
		return getClass().getSimpleName();
	}
}
