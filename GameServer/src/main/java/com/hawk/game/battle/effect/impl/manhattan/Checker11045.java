package com.hawk.game.battle.effect.impl.manhattan;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 11045】
- 【万分比】【11045】基地防守战斗时，自身伤害增加 XX%
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 在战斗开始时判定，满足条件后本场战斗全程生效
  - 该作用号仅在防守战斗且防守自身基地时生效
  - 该伤害加成为外围加成效果
    - 即实际伤害 = 基础伤害 *（1 + 其他伤害加成 +【本作用值】）
 * @author lwt
 * @date 2024年9月20日
 */
@BattleTupleType(tuple = Type.HURT)
@EffectChecker(effType = EffType.EFF_11045)
public class Checker11045 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type) && BattleConst.WarEff.CITY_DEF.check(parames.troopEffType)) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
