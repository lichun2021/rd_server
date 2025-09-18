package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_1635)
public class Checker1635 implements IChecker {
//	1635：1635 为固定值，不受军事值（三值成长影响），效果值 由const.xml effect1635BaseVal 控制 填 10= 0.1%
//
//			1.自身主战坦克（兵种类型 =2）出征士兵数量大于目标防御坦克（兵种类型 =1） 的X% X由const.xml effect1635Maxinum控制 填5000 = 50% 
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_2 || parames.type == SoldierType.TANK_SOLDIER_1) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
