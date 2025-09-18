package com.hawk.game.battle.effect.impl.hero1106;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 *   - 【万分比】【12469】个人战时，自身出征数量最多的直升机每触发 1 次黑鹰轰炸技能后，自身受到攻击时伤害减少 +XX.XX%【12469】（该效果可叠加，至多 X(effect12469Maxinum) 层）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 仅对个人战斗生效（包含个人进攻和个人防守）
  - 自身出征数量最多的直升机
    - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的直升机（兵种类型 = 4）生效（若存在多个直升机数量一样且最高，取等级高的）
  - 每触发 1 次黑鹰轰炸技能后
    - 【黑鹰轰炸】【id = 403】为直升机兵种技能
  - 自身受到攻击时伤害减少 +XX.XX%（该效果可叠加，至多 X 层）
    - 该伤害减少效果与其他伤害减少效果累乘计算；即实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 己方某伤害减少）*（1 - 本作用值伤害减少）
    - 注：此处有随敌方兵种类型，有内置系数
    - 实际数值 = 【作用值】* 叠加层数 *【兵种修正系数】
    - 各兵种修正系数读取const表，字段effect12469Adjust
      - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
        - 修正系数具体配置为万分比
    - 层数上限读取const表，字段分别为effect12469Maxinum
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12469)
public class Checker12469 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (BattleConst.WarEff.MASS.check(parames.troopEffType)) {
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
