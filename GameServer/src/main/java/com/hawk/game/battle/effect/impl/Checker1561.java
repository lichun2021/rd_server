package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.lianmengcyb.CYBORGConst.CYBORGState;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.ATK)
@EffectChecker(effType = EffType.CYBORG_1561)
public class Checker1561 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		boolean bfalse = parames.unity.getPlayer().getCYBORGState() == CYBORGState.GAMEING;
		if (bfalse) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
