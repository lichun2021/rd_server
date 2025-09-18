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
- 【万分比】【12231】战技持续期间，有 XX.XX% 概率将空军目标数有 2 个增至 3 个
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号仅在荣耀凯恩开启战技后，战技持续期间才生效
  - 此作用号机制是将荣耀凯恩的专属作用号【1657】在其开启战技期间的空军目标进行变更
  - 作用号数值即为判定概率
    - 每次发起攻击时独立判定
    - 实际概率取值区间为【0,100%】
  - 变更后的数量读取const表，字段effect12231ArmyNum
    - 配置格式：绝对值

【12232】
- 【万分比】【12232】触发磁暴聚能时，主战坦克超能攻击额外 +XX.XX%
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号机制是在荣耀凯恩的专属作用号【1656】触发时，额外增加其超能攻击
    - 同作用号【1656】，此加成仅在触发磁暴聚能的当回合生效
  - 此作用号为常规数值加成；即
    - 实际超能攻击 = 基础超能攻击 + 各类加成 +【本作用值】
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.EFF_12231)
public class Checker12231 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_1657) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		effPer = parames.unity.getEffVal(effType());
		return new CheckerKVResult(effPer, effNum);
	}
}
