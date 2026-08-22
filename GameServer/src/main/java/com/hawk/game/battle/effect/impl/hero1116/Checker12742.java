package com.hawk.game.battle.effect.impl.hero1116;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12742)
public class Checker12742 implements IChecker {
	/**
	 * 【12742~12743】
- 【万分比】【12742~12743】瞬闪换镜中狙击形态攻击+XX.XX%，紧凑形态超能攻击+XX.XX%
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 此作用号绑定瞬闪换镜作用号中狙击形态【12723】和紧凑形态【12725】
    - 作用号生效时，攻击和超能攻击与原作用号累加
    - 狙击形态
      - 实际攻击属性 = 基础属性*（1 + 各类加成 +【12723作用值】+【12742作用值】）
    - 紧凑形态
      - 实际超能攻击属性 = 基础属性*（1 + 各类加成 +【12725作用值】+【12743作用值】）
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12721) == 0) {
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
