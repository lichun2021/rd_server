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
@EffectChecker(effType = EffType.ARMOUR_11013)
public class Checker11013 implements IChecker {
	/**
	 * 11013	万分比	攻城车（兵种类型 = 7）攻击每命中1个敌方部队后，增加自身XX%生命加成（至多叠加X层）	
	 * 实际生命 = 基础生命 *（1 + 其他作用号 + 本作用值/10000）	"每攻击命中敌方1个目标，层数+1
	至多叠加层数读取const表，字段effect11013TimesLimit"
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.CANNON_SOLDIER_7) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
