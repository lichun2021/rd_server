package com.hawk.game.battle.effect.impl.hero1108;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
【12533】
- 【万分比】【12533】战技持续期间，重力缠绕使单位受到的传递伤害增加 XX.XX%【12533】
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号仅在艾莉克丝开启战技后，战技持续期间才生效
  - 此作用号机制是在作用号【12515】中，处于重力缠绕状态单位被受到传递伤害时，该伤害有额外加成
    - 该作用值与【12515作用值】累加计算，实际将【12515】的伤害公式变为
      - 其他单位受到伤害 = 主目标受到伤害 *（【12515作用值】+【本作用值】）*敌方兵种修正系数/10000
      - 敌方兵种修正系数沿用【12515】作用号中的
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL3)
@EffectChecker(effType = EffType.HERO_12533)
public class Checker12533 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12515) <= 0 || parames.solider.getEffVal(EffType.HERO_12521) <= 0 ) {
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