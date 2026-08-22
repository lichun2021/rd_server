package com.hawk.game.battle.effect.impl.hero1116;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12744)
public class Checker12744 implements IChecker {
	/**
	 * 【12744~12745】
- 【万分比】【12744~12745】战技持续期间，镜像残影获得的伤害减少+XX.XX%，绝对隐匿获得的伤害减少+XX.XX%
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 此作用号绑定镜像残影作用号中镜像残影减伤【12727】和绝对隐匿减伤【12728】，作用号生效时，伤害降低值与原作用号累加计算
    - 镜像残影减伤
      - 最终伤害 = 基础伤害 *（1 + 各类加成）*（1 - 各类减免）*（1 - (【12727作用值】+【12744作用值】）* 敌方兵种修正系数/10000）
      - 各固定值沿用作用号【12727】参数
    - 绝对隐匿减伤
      - 最终伤害 = 基础伤害 *（1 + 各类加成）*（1 - 各类减免）*（1 - (【12728作用值】+【12745作用值】）* 敌方兵种修正系数/10000）
      - 各固定值沿用作用号【12728】参数
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
