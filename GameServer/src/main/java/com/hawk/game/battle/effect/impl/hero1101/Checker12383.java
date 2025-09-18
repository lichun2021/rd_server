package com.hawk.game.battle.effect.impl.hero1101;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
/**
 *- 【万分比】【12383】触发爆裂弹片时，有 XX.XX% 的概率将目标敌方数由 2 个增至 3 个
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号机制为增加【12363】生效时可选取的敌方目标数量
  - 作用号数值即为判定概率
    - 每次发起攻击时独立判定
    - 实际概率取值区间为【0,100%】
  - 增加数量读取const表，字段effect12383AddNum
    - 配置格式：绝对值
    - 即【12363】生效时的实际目标数量 = effect12363TargetNum + 【本常量值】
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12383)
public class Checker12383 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12361) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

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
