package com.hawk.game.battle.effect.impl.red12031;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
- 【万分比】【12035】每释放1次【俯冲轰炸】技能后，轰炸机（兵种类型 = 3）伤害 +XX.XX%（至多叠加 10 层）
- 战报相关
  - 于战报中展示
  - 合并至精简战报中
- 该作用号对自身所有轰炸机部队均生效，不同id的轰炸机部队的释放效果和叠加层数独立计算
- 【俯冲轰炸】为轰炸机兵种专属兵种技能，技能组id = 303
- 此伤害加成与其他伤害加成累加计算；即 实际伤害 = 基础伤害*（1 + 各类伤害加成 +【本作用值】）
- （至多叠加 10 层）
  - 效果记录在己方部队身上，数值直接叠加；限制该作用号叠加层数上限
    - 层数上限读取const表，字段effect12035Maxinum
      - 配置格式：绝对值
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12035)
public class Checker12035 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.PLANE_SOLDIER_3) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}

	

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}