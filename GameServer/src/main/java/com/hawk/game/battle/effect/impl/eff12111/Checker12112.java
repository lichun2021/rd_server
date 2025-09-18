package com.hawk.game.battle.effect.impl.eff12111;

import org.hawk.os.HawkRand;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;

/**
 * 【12112】
- 【万分比】【12112】奇袭：战斗开始时，己方有 XX% 概率额外获取1个战斗回合
  - 战报相关
    - 于战报中隐藏 
    - 不合并至精简战报中
  - 该判定于战斗开始前，判定成功后本场战斗生效
    - 注：集结战斗时，不同玩家独立判定（判定成功后对己方所有部队（兵种类型：1~8）生效）
  - 正常战斗回合为 50 回合
    - 本作用号效果为 50 回合结束后，额外进行1个回合
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12112)
public class Checker12112 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {

		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type)) {
			effPer = parames.unity.getEffVal(effType());

			Integer val = (Integer) parames.getPlayerExtryParam(getSimpleName());
			if (val == null) {
				val = effPer >= HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE) ? 1 : 0;
				parames.putPlayerExtryParam(getSimpleName(), val);
			}
			parames.solider.setExtround12112(val);
		}
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
