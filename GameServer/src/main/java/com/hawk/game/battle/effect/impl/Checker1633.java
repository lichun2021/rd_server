package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

//作用号需求：
//
//
//effectid=1633
//
//触发条件：出征或警戒时，采矿车（1007）占部队比例超过10%时（条件写死）
//
//触发效果：每N回合（取const中effect1633TimesLimit字段）给队伍 最前排 套上采矿车最大血量 XX% 的护盾，带有护盾的目标被攻击时，优先扣除护盾血量，护盾在N回合结束时清零，下一回合开始时重新套。
//特殊处理：多个1633存在时，取数值最高的生效。
//
// 
//
//备注：若采矿车成为最前排，则给自己套盾。
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.SUPER_SOLDIER_1633)
public class Checker1633 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.CANNON_SOLDIER_8) {
			double footCount = parames.getArmyTypeCount(SoldierType.CANNON_SOLDIER_8);
			double per = 0.1 - parames.unity.getEffVal(EffType.HERO_1640) * GsConst.EFF_PER;
			if (footCount / parames.totalCount >= per) {
				effPer = parames.unity.getEffVal(effType());
			}
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
