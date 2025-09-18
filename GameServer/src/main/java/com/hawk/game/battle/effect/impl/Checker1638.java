package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 作用号 1638

触发条件

1.自身狙击兵（兵种类型 =6）出征士兵数量>=目标突击步兵（兵种类型 =5） 的X% X由const.xml effect1638Per控制 填8000 = 80% 

2.则对所有满足的目标突击步兵（包含集结）且突击步兵归属玩家的身上携带【1638】.



触发效果

突击步兵 可获得来自多个狙击兵目标归属玩家的身上携带【1638】强化连击概率，这些【1638】可叠加,有上限，实际触发时：是将突击步兵的单回合攻击2次（battle_soldier 攻击频率动态调整 attackRound：500 下调至333）变为3次

突击步兵强化连击概率 = Min（【1638】，X) +其他强化连击概率加成

X由const.xml effect1638Maxinum控制 填2000= 20% 



战报显示

突击步兵兵种信息“！”显示文本 ：本次战斗，强化连击触发 X 次
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_1638)
public class Checker1638 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_5 && parames.unity.getEffVal(effType()) > 0) { // 6 并且携带1637
			double marchCnt = parames.unity.getArmyInfo().getFreeCnt() - parames.unity.getArmyInfo().getShadowCnt();
			for (BattleUnity unity : parames.unityList) {
				if (unity.getEffVal(effType()) > 0 && unity.getArmyInfo().getType() == SoldierType.FOOT_SOLDIER_6) {// 5 并且携带1637
					double marchCnt6 = unity.getArmyInfo().getFreeCnt() - unity.getArmyInfo().getShadowCnt();
					if (marchCnt6 / marchCnt >= ConstProperty.getInstance().getEffect1638Per()) {
						effPer += unity.getEffVal(effType());
					}
				}
			}
		}
		effPer = Math.min(effPer, ConstProperty.getInstance().getEffect1638Maxinum());
		return new CheckerKVResult(effPer, effNum);
	}
}
