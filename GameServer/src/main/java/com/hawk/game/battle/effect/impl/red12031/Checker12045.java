package com.hawk.game.battle.effect.impl.red12031;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
【12042~12049】
- 【万分比】【12042】主战坦克（兵种类型 = 2）攻击、防御和生命
- 【万分比】【12043】防御坦克（兵种类型 = 1）攻击、防御和生命
- 【万分比】【12044】直升机（兵种类型 = 4）攻击、防御和生命
- 【万分比】【12045】轰炸机（兵种类型 = 3）攻击、防御和生命
- 【万分比】【12046】狙击兵（兵种类型 = 6）攻击、防御和生命
- 【万分比】【12047】突击步兵（兵种类型 = 5）攻击、防御和生命
- 【万分比】【12048】采矿车（兵种类型 = 8）攻击、防御和生命
- 【万分比】【12049】攻城车（兵种类型 = 7）攻击、防御和生命
  - 注：上述作用号均为外围属性加成；即实际生命/防御/攻击 = 基础属性*（1 + 基础属性加成）*（1 + 各类属性加成 + 【本作用值】）
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
 */

@BattleTupleType(tuple = { Type.ATK, Type.DEF, Type.HP })
@EffectChecker(effType = EffType.EFF_12045)
public class Checker12045 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.PLANE_SOLDIER_3) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}