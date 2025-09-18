package com.hawk.game.battle.effect.impl.manhattan;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 【11051~11059】
- 【绝对值】【11051~11059】全兵种或对应兵种超能强化 +XX
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中-各兵种单独出一套新属性展示
  - 对应如下
    - 11051~11058：指定某兵种生效，分别对应兵种类型1~8
    - 11059：全兵种生效（兵种类型1~8）
 */
@BattleTupleType(tuple = Type.ATKFIRE_MHT)
@EffectChecker(effType = EffType.EFF_11056)
public class Checker11056 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_6) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
