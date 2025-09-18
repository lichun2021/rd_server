package com.hawk.game.battle.effect.impl.ssstalent;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 
【12311】
- 【万分比】【12311】出征或驻防时，突击步兵额外伤害 +XX.XX%（此效果对敌方近战单位翻倍）
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此加成为常规外围属性加成效果；即
    - 实际伤害 = 基础伤害*（1 + 各类加成 +【本作用值】）
  - 作用号实际数值 = 【本作用值】*目标兵种修正系数/10000
  - 另外随攻击命中的敌方部队类型不同，有个单独的修正系数
  - 各兵种修正系数读取const表，字段effect12311DamageAdjust
    - 配置格式：兵种1修正系数_兵种2修正系数_......_兵种8修正系数
      - 各修正系数配置为万分比

【12312】
- 【万分比】【12312】战技持续期间，触发蓄能炮时，有 XX.XX% 的概率使攻击轮次由 3 轮增至 4 轮
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号仅在卡尔开启战技后，战技持续期间才生效
  - 此作用号机制是将卡尔的专属作用号【12202】的攻击轮次进行变更
  - 作用号数值即为判定概率
    - 每次发起攻击时独立判定
    - 实际概率取值区间为【0,100%】
  - 变更后的数量读取const表，字段effect12312AtkNum
    - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12312)
public class Checker12312 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity.getEffVal(EffType.HERO_12202) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		if ( parames.type == SoldierType.FOOT_SOLDIER_5) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
	
	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
