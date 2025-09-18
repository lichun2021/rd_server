package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**作用号 1637

触发条件
1.自身突击步兵（兵种类型 =5）出征士兵数量>=目标狙击兵（兵种类型 =6） 的X% X由const.xml effect1637Per控制 填8000 = 80% 
2.则对所有满足的目标狙击兵（包含集结）且狙击兵归属玩家的身上携带【1637】.
触发效果
狙击兵 可获得来自多个突击步兵目标归属玩家的身上携带【1637】闪避概率，这些【1637】可叠加,有上限，该闪避可以闪避任何伤害，类似作用号【1313】的效果
狙击兵闪避概率 = Min（【1637】，X) +其他闪避概率
X由const.xml effect1637Maxinum控制 填1000= 10% 
战报显示
狙击兵兵种信息“！”显示文本 ：本次战斗，强化隐匿触发 X 次*/

@BattleTupleType(tuple = Type.DODGE)
@EffectChecker(effType = EffType.HERO_1637)
public class Checker1637 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_6 && parames.unity.getEffVal(effType()) > 0) { // 6 并且携带1637
			double marchCnt6 = parames.unity.getArmyInfo().getFreeCnt() - parames.unity.getArmyInfo().getShadowCnt();
			for (BattleUnity unity : parames.unityList) {
				if (unity.getEffVal(effType()) > 0 && unity.getArmyInfo().getType() == SoldierType.FOOT_SOLDIER_5) {// 5 并且携带1637
					double marchCnt5 = unity.getArmyInfo().getFreeCnt() - unity.getArmyInfo().getShadowCnt();
					if (marchCnt5 / marchCnt6 >= ConstProperty.getInstance().getEffect1637Per()) {
						effPer += unity.getEffVal(effType());
					}
				}
			}
		}
		effPer = Math.min(effPer, ConstProperty.getInstance().getEffect1637Maxinum());
		return new CheckerKVResult(effPer, effNum);
	}
}
