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
@EffectChecker(effType = EffType.ARMOUR_11003)
public class Checker11003 implements IChecker {
	/**
	 * 3	11003	万分比	主战坦克（兵种类型 = 2）攻击每命中1个不同的目标后，自身暴击伤害增加XX%（至多叠加X层）	
	 * 实际伤害 = 基础伤害 *（1 + 其他伤害加成 + 本作用值/10000）	"不同目标定义：战斗中的独立战斗单位，即只有同玩家同兵种id的才算同一目标，
	 * 其他情况都是不同的目标单位（这里只算部队-兵种类型 = 1~8的）
	 * 至多叠加层数读取const表，字段effect11003TimesLimit"	不展示	不展示													
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_2) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
