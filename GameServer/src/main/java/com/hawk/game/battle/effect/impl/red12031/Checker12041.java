package com.hawk.game.battle.effect.impl.red12031;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
- 【万分比】【12041】集结战斗时，自身出征数量最多的攻城车（兵种类型 = 7）部队，攻击命中敌方空军部队后，降低其伤害加成 +XX.XX%（持续 2 回合）
- 战报相关
  - 于战报中展示
  - 不合并至精简战报中
- 该作用号仅对集结战斗生效（包含集结进攻和集结防守）
- 该作用号效果仅对玩家出征时数量最多的攻城车（兵种类型 = 7）生效（若存在多个采矿车数量一样且最高，取等级高的）
- 此伤害降低效果与其他伤害加成累加计算；即 实际伤害 = 基础伤害*（1 + 各类伤害加成 - 【本作用值】）
- 空军部队包含直升机（兵种类型 = 4）和轰炸机（兵种类型 = 3）
- 该效果由被攻击附加后，持续 2 个回合（本回合被附加到下回合开始算 1 回合）
  - 该效果无法叠加，若敌方已处于该效果中，则无法继续附加（不进行任何处理）
  - 持续回合数读取const表，字段effect12041ContinueRound
    - 配置格式：绝对值
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12041)
public class Checker12041 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.CANNON_SOLDIER_7 && isPlan(parames.tarType) && BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}

	// @Override
	// public boolean tarTypeSensitive() {
	// return false;
	// }
}