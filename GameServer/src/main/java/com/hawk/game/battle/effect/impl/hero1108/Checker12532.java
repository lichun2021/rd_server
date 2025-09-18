package com.hawk.game.battle.effect.impl.hero1108;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
【12532】
- 【万分比】【12532】触发歼灭突袭时，有 【effect12532BaseVaule +XX.XX%【12532】*敌方空军单位数】 的概率增加 1 个追击目标（敌方空军单位计数时至多取 10（effect12532CountMaxinum） 个）
  - 注：本技能与薇拉作用号【12481】基本完全一致
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 此作用号绑定空中缠斗作用号【12513】，作用号生效时，额外触发以下效果
  - 有 【固定值 + XX.XX%*敌方空军单位数】 的概率将目标敌方数由 1 个增至 2 个
    - 该作用号固定数值读取const表，字段effect12532BaseVaule
      - 配置格式：万分比
    - 此作用号机制为改变【12513】作用号生效时可选取的目标的数量
      - 特殊的：此处概率性数值可支持高于100%
        - 以210%为例：则表示必定 +2个目标，且有10%概率额外再 +1个目标
      - 多目标选择规则
        - 作用号生效后，优先在敌方轰炸机中随机选择 X 个目标，若敌方无可选单位或者不足X个单位，则在敌方直升机单位中随机选择目标，直至敌方目标单位不足或选取目标总和到X。
      - 每次发起攻击时独立判定
  - （敌方空军单位计数时至多取 10 个）
    - 该计数有最高值限制，读取const表，字段effect12532CountMaxinum
      - 配置格式：绝对值
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL3)
@EffectChecker(effType = EffType.HERO_12532)
public class Checker12532 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12513) <= 0) {
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