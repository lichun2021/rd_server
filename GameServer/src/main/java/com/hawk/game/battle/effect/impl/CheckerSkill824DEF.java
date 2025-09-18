package com.hawk.game.battle.effect.impl;

import java.util.Objects;

import org.apache.commons.lang.math.NumberUtils;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.DEF)
@EffectChecker(effType = EffType.PLANT_SOLDIER_SKILL_824)
public class CheckerSkill824DEF implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.solider.isTank()) {
			Object object = parames.getLeaderExtryParam(getSimpleName());
			if (Objects.nonNull(object)) {
				effPer = (int) object;
			} else {
				int cen  = 0;
				for (BattleUnity unit : parames.unityList) {
					BattleSoldier soldier = unit.getSolider();
					if(soldier.getType() == SoldierType.CANNON_SOLDIER_8 && soldier.getSoldierCfg().isPlantSoldier()){
						BattleSoldierSkillCfg skill824 = soldier.getSkill(PBSoldierSkill.SOLDIER_SKILL_824);
						String p2 = skill824.getP2();
						effPer += NumberUtils.toInt(p2);
						cen ++;
						if(cen >= skill824.getP1IntVal()){
							break;
						}
					}
				}
				parames.putLeaderExtryParam(getSimpleName(), effPer);
			}
		}

		return new CheckerKVResult(effPer, effNum);
	}
}