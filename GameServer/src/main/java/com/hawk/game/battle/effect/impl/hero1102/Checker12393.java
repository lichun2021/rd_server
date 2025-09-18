package com.hawk.game.battle.effect.impl.hero1102;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 【12393】
- 【万分比】【12223】任命在战备参谋部时，自身部队剩余数量超过 70% 时，自身所有单位造成伤害 +XX.XX%
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 在战斗每回合判定，满足条件后本轮战斗生效
  - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
  - （自身部队剩余数量超过 70% ）
    - 数值读取const表，字段effect12393Maxinum
      - 配置格式：万分比
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12393)
public class Checker12393 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type)) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
