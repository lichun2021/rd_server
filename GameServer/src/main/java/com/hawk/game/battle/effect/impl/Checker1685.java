package com.hawk.game.battle.effect.impl;

import java.util.HashMap;
import java.util.Map;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.EFF_1685)
public class Checker1685 implements IChecker {
	/**
	 - effectid：1685
	- 效果：集结战斗时，全部友军的伤害减免+XX%，上限XX层（const表 effect1685Maxinum）
	- 这里的伤害减免和其他的免伤效果为连乘关系，即：（1-减伤A）*（1-减伤B）
	- 集结类光环，可参考作用号：1605	
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			Object object = parames.getLeaderExtryParam(getSimpleName());
			if (object != null) {
				effPer = (int) object;
			} else {
				effPer = calVal(parames);
				parames.putLeaderExtryParam(getSimpleName(), effPer);
			}
		}
		return new CheckerKVResult(effPer, effNum);
	}

	private int calVal(CheckerParames parames) {
		Map<String, Integer> valMap = new HashMap<>();
		for (BattleUnity unity : parames.unityList) {
			if (valMap.containsKey(unity.getPlayerId())) {
				continue;
			}
			int effvalue = unity.getEffVal(effType());
			valMap.put(unity.getPlayerId(), effvalue);
		}

		int result = valMap.entrySet().stream()
				.sorted(((item1, item2) -> {
					int compare = item2.getValue().compareTo(item1.getValue());
					return compare;
				}))
				.filter(ent -> ent.getValue() > 0)
				.limit(ConstProperty.getInstance().getEffect1685Maxinum())
				.mapToInt(item -> item.getValue().intValue())
				.sum();
		
		return result;
	}
}
