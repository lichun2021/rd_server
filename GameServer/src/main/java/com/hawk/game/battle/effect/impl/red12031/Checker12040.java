package com.hawk.game.battle.effect.impl.red12031;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
- 【万分比】【12040】集结战斗时，自身出征数量最大的采矿车（兵种类型 = 8）部队，每受到1次攻击，则使自身及身后最近的 4 个友军单位生命加成 +XX.XX%（至多叠加 20 层）
- 战报相关
  - 于战报中展示
  - 不合并至精简战报中
- 该作用号仅对集结战斗生效（包含集结进攻和集结防守）
- 该作用号效果仅对玩家出征时数量最多的采矿车（兵种类型 = 8）生效（若存在多个采矿车数量一样且最高，取等级高的）
- 为自身及友军增加生命属性，同与常规外围属性加成累加计算；即实际生命 = 基础生命*（1 + 各类加成 + 【本作用值】）
- 该效果由被攻击附加后，持续至本场战斗结束
- 若身后友军单位不足 4 个，则有多少取多少；若身后无友军单位，则不给友军单位提供增益
- 可生效友军单位数量读取const表，字段effect12040Nums
  - 配置格式：绝对值
- 集结中存在多个此作用号时，可以同时生效
- 效果记录在己方部队身上，数值直接叠加；限制该作用号叠加层数上限
  - 先到先叠，叠满 10 层后则不再变化
  - 层数上限读取const表，字段effect12040Maxinum
    - 配置格式：绝对值
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12040)
public class Checker12040 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.CANNON_SOLDIER_8)) {
			return CheckerKVResult.DefaultVal;
		}
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.CANNON_SOLDIER_8 && BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}