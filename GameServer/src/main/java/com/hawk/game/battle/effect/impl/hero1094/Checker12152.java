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
 * - 【万分比】【12152】任命在战备参谋部时，主战坦克攻击命中空军后，自身暴击几率 +XX.XX%（该效果可叠加，至多 50 层）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 空军部队类型包含有：直升机（兵种类型 = 4）和轰炸机（兵种类型 = 3）
  - 暴击几率与其他暴击几率叠加计算；即 实际暴击几率 = 基础暴击几率 + 其他加成 +【本作用值】
    - 注：暴击几率为百分比判定属性，其最终取值合法区间为【0,100%】
  - 若己方存在以主战坦克为母体部队的幻影部队，则此作用号对该幻影部队也生效
  - （该效果可叠加，至多 50 层）
    - 每次攻击命中空军后，提供 1 层数值效果
    - 效果记录在己方部队身上，数值直接叠加；限制该作用号叠加层数上限
      - 注：这里限制的是叠加层数上限，达到层数上限后本次战斗持续生效到战斗结束，后续再触发时不再叠加
      - 层数上限读取const表，字段effect12152Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = { Type.SOLDIER_SKILL })
@EffectChecker(effType = EffType.HERO_12152)
public class Checker12152 implements IChecker {
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
