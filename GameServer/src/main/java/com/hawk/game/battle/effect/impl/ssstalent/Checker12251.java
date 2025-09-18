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
- 【12251】
- 【万分比】【12251】突击步兵越过防御坦克的额外攻击，有 XX.XX% 概率将最大发动次数由 3 次增至 4 次
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号机制是将埃托莉亚的专属作用号【1669】的每回合最大发动次数进行变更
  - 作用号数值即为判定概率
    - 每次发起攻击时独立判定
    - 实际概率取值区间为【0,100%】
  - 变更后的数量读取const表，字段effect12251AtkTimes
    - 配置格式：绝对值

【12252】
- 【万分比】【12252】狙击兵造成点燃伤害时，伤害额外 +XX.XX%
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号机制是为埃托莉亚的专属作用号【1670】，狙击兵的点燃伤害额外增加伤害加成
  - 此伤害加成为额外伤害加成效果，与其他作用号累加计算
    - 此伤害加成仅对【1670】作用号造成的点燃伤害生效，其他攻击造成的伤害均无效
    - 即 实际伤害 = 基础伤害*（1 + 其他作用号加成 +【本作用值】）

【12253】
- 【万分比】【12253】战技持续期间，超能护盾效果最高可抵挡 XX.XX% 的伤害和死亡效果，且最多抵抗次数由 3 次增至 4 次
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号机制是将埃托莉亚的专属作用号【1672】的首次伤害抵抗效果进行数值变更
    - 最终数值【1672】 = 【12253】*兵种修正系数/10000
  - 兵种修正系数按狙击兵（兵种类型 = 6）和突击步兵（兵种类型 = 5）进行配置修正，读取const表，字段effect12253SoldierAdjust
    - 配置格式：兵种类型1_修正系数1,兵种类型2_修正系数2
      - 修正系数为万分比
  - 且将其最多抵抗次数进行变更，同时按兵种类型进行修正
    - 变更后的数量读取const表，字段effect12253HoldTimes
      - 配置格式：兵种类型1_变更后次数1,兵种类型2_变更后次数2
        - 变更次数为绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.EFF_12251)
public class Checker12251 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_1669) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		effPer = parames.unity.getEffVal(effType());
		return new CheckerKVResult(effPer, effNum);
	}
}
