package com.hawk.game.battle.effect.impl.manhattan;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * - 【万分比】【12641~12642】进攻战斗开始时，随机选中某指挥官，对其后排所有部队进行一轮轰炸（伤害率：XX.XX%【12641】），
 * 并降低目标防御、血量XX.XX%【12642】，持续X（effect12641ContinueRound）回合（效果不叠加，优先选择未被选中单位）->针对敌方兵种留个内置修正系数（effect12641SoldierAdjust）
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.EFF_12642)
public class Checker12642 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity.getEffVal(EffType.EFF_12641) <= 0) {
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
