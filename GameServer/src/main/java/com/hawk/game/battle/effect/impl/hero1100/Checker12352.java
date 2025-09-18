package com.hawk.game.battle.effect.impl.hero1100;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
【12352】
- 【万分比】【12352】在开启火力全开模式时，额外为目标友军提供 XX.XX% 的超能攻击加成
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号机制为【12333】生效的目标单位额外增加属性加成，在【12333】确定目标后挂上，且随【12333】消失
    - 且同作用号【12333】，此效果无法叠加
  - 此加成为外围属性加成效果，与其他作用号累加计算
    - 即 实际属性 = 基础属性*（1 + 其他作用号加成 +【本作用值】）
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12352)
public class Checker12352 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12333) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		effPer = parames.unity.getEffVal(effType());
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}