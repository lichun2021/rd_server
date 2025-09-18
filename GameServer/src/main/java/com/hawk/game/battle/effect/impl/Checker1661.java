package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.DEF)
@EffectChecker(effType = EffType.HERO_1661)
public class Checker1661 implements IChecker {

	/**
	 * 【1661】
	触发逻辑：
	战斗前判断敌人主战坦克的排数（兵种类型 =2 ），给我方空军增加 【1661】*排数的空军防御（兵种类型 = 3&4）
	
	最终空军防御加成 = 1+其他百分比防御加成+【1661】*Min(对方主战排数,X）
	X由const.xml  effect1658Num 控制，填10为最大生效10排
	
	若玩家携带了 泰能空军 即 士兵类型 = 3 和 4 中的battle_soldier level  13 和 14 士兵，该排部队，享受上述双倍加成
	
	最终空军防御加成 = 1+其他百分比防御加成+【1661】*Min(对方主战排数,X）*2
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity.getEffVal(effType()) <= 0) {
			return CheckerKVResult.DefaultVal;
		}
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.PLANE_SOLDIER_3 || parames.type == SoldierType.PLANE_SOLDIER_4) {
			long num = parames.tarStatic.getArmyList().stream()
					.filter(u -> u.getType() == SoldierType.TANK_SOLDIER_2)
					.count();
			effPer = (int) (parames.unity.getEffVal(effType()) * Math.min(num, ConstProperty.getInstance().getEffect1661Num()));
			if (parames.solider.getSoldierCfg().isPlantSoldier()) {
				effPer = effPer * 2;
			}
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
