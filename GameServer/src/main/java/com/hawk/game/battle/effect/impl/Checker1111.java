package com.hawk.game.battle.effect.impl;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.DEF)
@EffectChecker(effType = EffType.SOLDIER_1111)
public class Checker1111 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		Player unitPlayer = parames.unity.getPlayer();
		// 跨服且不在副本中生效
		if (unitPlayer.isCsPlayer() && HawkOSOperator.isEmptyString(unitPlayer.getDungeonMap())) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
