package com.hawk.game.battle.effect.impl.ssstalent;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 
【12291】
- 【万分比】【12291】触发坦克对决时，伤害额外 +XX.XX%，且有 XX.XX% 概率将目标数量由 1 个增至 2个
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号机制是为米卡的专属作用号【12131】提供额外伤害加成效果
  - 此伤害加成为额外伤害加成效果，与其他作用号累加计算
    - 此伤害加成仅对【12131】作用号的攻击行为生效，其他攻击造成的伤害均无效
    - 即 实际伤害 = 基础伤害*（1 + 其他作用号加成 +【本作用值】）

【12292】
- 【万分比】【12292】触发坦克对决时，伤害额外 +XX.XX%，且有 XX.XX% 概率将目标数量由 1 个增至 2个
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号机制是将米卡的专属作用号【12131】变更为范围攻击效果
  - 作用号数值即为判定概率
    - 每次发起攻击时独立判定
    - 实际概率取值区间为【0,100%】
  - 变更后的数量读取const表，字段effect12292AtkNum
    - 配置格式：绝对值
  - 随机规则
    - 作用号生效后，优先在敌方主战坦克中随机 2 个不同目标，若敌方无主战坦克，则在敌方防御坦克中随机补上剩余目标，若敌方也无防御坦克，则判定失去攻击目标，此次攻击失效或因目标数不足只能攻击1个目标

【12293】
- 【万分比】【12293】战技持续期间，主战坦克每受到 1 次攻击后，暴击伤害 +XX.XX%（该效果可叠加，至多 100 层）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号仅在米卡开启战技后，战技持续期间才生效
  - 该作用号在计算时与其他作用号累加计算
    - 即 实际暴击伤害 = 基础暴击伤害 + 其他作用号加成 + 【本作用值】
  - （该效果可叠加，至多 100 层）
    - 该作用号可叠加，受叠加次数上限限制
      - 层数上限读取const表，字段effect12293Maxinum
        - 配置格式：绝对值

 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.EFF_12291)
public class Checker12291 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12131) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		effPer = parames.unity.getEffVal(effType());
		return new CheckerKVResult(effPer, effNum);
	}
}
