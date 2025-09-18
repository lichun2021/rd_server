package com.hawk.game.battle.effect.impl;

import java.util.Objects;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_1653)
public class Checker1653 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.tarType)) {
			String playerId = parames.unity.getPlayer().getId();
			BattleUnity maxArmy = null;
			for (SoldierType type : SoldierType.values()) {
				if (!isSoldier(type)) {
					continue;
				}
				BattleUnity temp = parames.getPlayerMaxFreeArmy(playerId, type);
				if(Objects.isNull(temp)){
					continue;
				}
				if (Objects.isNull(maxArmy) || temp.getFreeCnt() > maxArmy.getFreeCnt()) {
					maxArmy = temp;
				}
			}
			if (maxArmy == parames.unity) {
				effPer = parames.unity.getEffVal(effType());
			}

		}
		return new CheckerKVResult(effPer, effNum);
	}
}
