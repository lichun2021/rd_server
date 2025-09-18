package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.ARMOUR_11008)
public class Checker11008 implements IChecker {
	/**
	 * 11007	万分比	狙击兵（兵种类型 = 6）在自身后排存在其他兵种时，增加自身XX%伤害加成	
	 * 实际伤害 = 基础伤害 *（1 + 其他伤害加成 + 本作用值/10000）	"自身后排的兵种只算部队-兵种类型 = 1~8的
	个人和集结战斗都生效"
	11008	万分比	狙击兵（兵种类型 = 6）在自身后排不存在其他兵种时，
	增加自身XX%生命加成	实际生命 = 基础生命 *（1 + 其他作用号 + 本作用值/10000）	
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_6) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
