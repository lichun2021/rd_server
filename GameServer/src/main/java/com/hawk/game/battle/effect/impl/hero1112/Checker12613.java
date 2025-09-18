package com.hawk.game.battle.effect.impl.hero1112;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * - 【万分比】【12613】扰乱效应：【雷感状态】的单位每次造成伤害时，受到一次雷感伤害（伤害率：XX.XX%【12613】，每个存在的【雷感状态】在1回合内至多触发X（effect12613Maxinum）次）
- 战报相关
  - 于战报中隐藏
  - 不合并至精简战报中
- 【雷感状态】的单位每次造成伤害时，受到一次雷感伤害（伤害率：XX.XX%，
  - （伤害率: XX.XX%，以自身突击步兵为基础计算，不算作突击步兵直接攻击
    - 即 实际伤害 = 伤害率 * 基础伤害 *（1 + 各类加成）
- 每个存在的【雷感状态】在1回合内至多触发X次）
  - 同【雷感状态】叠加逻辑，多个突击步兵玩家造成的【雷感状态】效果独立计算，读取自身英雄作用号
  - 每个玩家【雷感状态】造成的雷感伤害次数独立计算，各自读取自身英雄次数上限
  - 触发上限读取const表，字段effect12613Maxinum
    - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12613)
public class Checker12613 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity.getEffVal(EffType.HERO_12611) <= 0) {
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
