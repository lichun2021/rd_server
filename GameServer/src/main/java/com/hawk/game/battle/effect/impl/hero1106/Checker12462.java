package com.hawk.game.battle.effect.impl.hero1106;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 *   - 【12462~12463】缠斗状态 下，己方直升机以耗损 XX.XX% 的伤害效率的代价，在受到缠斗目标伤害时，额外减少 +XX.XX%
    - 【12462】缠斗方造成伤害效率降低；即实际造成伤害时 ；即实际造成伤害 = 基础伤害*（1 + 己方各类伤害加成）*（1 - 敌方某伤害减少）*（1 - 【本作用值】）
      - 这是个给自身施加的debuff效果，使自身造成的所有伤害降低
    - 【12463】缠斗方受到被缠斗方伤害时，使本次伤害减少；该作用号为伤害减少效果，与其他作用号累乘计算，即 
      - 最终伤害 = 基础伤害 *（1 + 各类加成）*（1 - 各类减免）*（1 - 【本作用值】）
  - （多个薇拉同时存在时，至多有 2 个直升机单位获得上述效果）
    - 单玩家拥有此作用号时，只能提供 1 层效果；集结中存在多个此作用号时，可以叠加
    - 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
      - 注：这里限制的是生效层数上限，若集结中层数超出此上限，取作用号数值高的；即这里只能有 2 个玩家携带的作用号生效（在战前判定即可，战中不再变化）
      - 层数上限读取const表，字段effect12461Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12462)
public class Checker12462 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12461) <= 0) {
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
