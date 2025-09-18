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
@EffectChecker(effType = EffType.ARMOUR_11004)
public class Checker11004 implements IChecker {
	/**
	 * 4	11004	万分比	防御坦克（兵种类型 = 1）每受到1次攻击后，受到伤害减少XX%（至多叠加X层）	
	 * 实际受到伤害 = 基础伤害*（1 - 其他作用号减免）*（1 - 本作用值/10000）	
	 * 至多叠加层数读取const表，字段effect11004TimesLimit	不展示	不展示													
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_1) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
