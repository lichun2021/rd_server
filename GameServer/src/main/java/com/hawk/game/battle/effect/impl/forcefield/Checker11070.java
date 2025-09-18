package com.hawk.game.battle.effect.impl.forcefield;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 *   - 作用号：11070 = 星穹护盾值上限
    - 涛叔建议：当前作用号输入值底层无法超过32位，如果需要超过64位，应多留几个作用号以备不时之需。因此，现将11070~11079划给星穹护盾玩法
  - 战斗开始前，为我方部队提供星穹护盾值。
- 每场战斗开始前
  - 计算每个玩家的星穹护盾值上限
    - 玩家的护盾值上限 = 求和（ effect.11070 + ……其他护盾值作用号 ）
    - 部队的护盾值上限 = 玩家的护盾值上限 * MIN（ 1, 该玩家士兵数/100000 ）
  - 计算部队总护盾值上限（单人、集结通用）
    - 部队的护盾值上限 = 求和（我方所有参战玩家的护盾值上限）
    - 战前算得的护盾值上限，在战斗中将不再发生变化
  - 在战斗开始时，将部队的护盾值补满。
- 当单位即将受到伤害时，如果部队拥有护盾值
  - 护盾承伤 = MIN（剩余盾值，伤害值*0.9）
  - 生命承伤 = 伤害值 - 护盾承伤
  - 士兵战损向上取整，一个士兵哪怕只是被蹭掉了一丁点血皮子，也要去死。与现有逻辑一致
- 需要提前考虑之后的护盾技能需求
  - 技能形式包括但不限于：高效削盾、增强盾效、存在护盾时提供强化、盾破时触发效果；但是会谨慎处理护盾回复类效果。
 */
@BattleTupleType(tuple = Type.FORCE_FIELD)
@EffectChecker(effType = EffType.EFF_11070)
public class Checker11070 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		String pid = parames.unity.getPlayerId();
		BattleUnity max = parames.getPlayeMaxMarchArmy(pid);

		if (parames.unity != max) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type)) {
			effPer = parames.unity.getEffVal(effType());
			parames.solider.setForceFieldMarch(parames.unitStatic.getPlayerArmyCountMapMarch().get(pid));
		}

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
