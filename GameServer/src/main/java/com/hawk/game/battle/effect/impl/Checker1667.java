package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleConst.WarEff;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.battle.sssSolomon.ISSSSolomonPet;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;

/**
 * Skill1081
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_1667)
public class Checker1667 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.solider.getShadowCnt() > 0 && parames.troopEffType != WarEff.NO_EFF) {
			effPer = parames.unity.getEffVal(effType());
			if (effPer > 0 && parames.solider.getSolomonPet() == null) {
				int shadowCnt = (int) ((parames.solider.getFreeCnt() - parames.solider.getShadowCnt()) * GsConst.EFF_PER * effPer);
				shadowCnt = Math.max(1, shadowCnt);
				ISSSSolomonPet.createPet(parames.solider, parames.unity.getPlayer(), parames.solider.getSoldierCfg(), shadowCnt, shadowCnt);
			}
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
