package com.hawk.game.battle.effect.impl.hero1094;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * - 【万分比】【12153】任命在战备参谋部时，主战坦克攻击命中空军且暴击后，降低其【XX.XX% + 敌方空军单位数*XX.XX%】的伤害效果（敌方空军单位计算时至多取 5 个；该效果不可叠加，持续 2 回合）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 空军部队类型包含有：直升机（兵种类型 = 4）和轰炸机（兵种类型 = 3）
    - 注：这里须命中空军且本次攻击暴击时，才会触发
  - 此伤害降低效果为debuff效果，记在被攻击方身上
    - 即被攻击方挂上此debuff效果后，被攻击方的单位在此debuff效果生效期间，攻击其他单位时都会受到此debuff效果影响
  - 注：若命中的目标为某荣耀所罗门的幻影部队的母体部队，此debuff效果也给其幻影部队挂上
  - 降低伤害为外围减伤效果，与其他伤害加成累乘计算；即 敌方实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 某作用值伤害减免）*（1 - 【本作用值】）
  - 该效果不可叠加，可刷新重置
    - 即在此debuff生效期间，若敌方单位又受到此debuff效果，则刷新其debuff数值且重置其持续回合数
  - 持续回合数读取const表，字段effect12153ContinueRound
    - 注：由被附加开始到当前回合结束，算作 1 回合
 */
@BattleTupleType(tuple = { Type.SOLDIER_SKILL })
@EffectChecker(effType = EffType.HERO_12153)
public class Checker12153 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_2) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);

	}

}
