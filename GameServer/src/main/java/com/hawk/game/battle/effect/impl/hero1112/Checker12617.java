package com.hawk.game.battle.effect.impl.hero1112;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * - 【万分比】【12617】个人战时，每次触发磁流强化时，自身突击步兵受到的伤害减少XX.XX%【12617】（该效果可叠加，至多X（effect12617Maxinum）层）->针对所有敌方兵种留个内置系数（effect12617SoldierAdjust）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 个人战时，每次触发磁流强化时
    - 仅个人战生效，集结无效
    - 磁流强化有叠加上限，就算达到叠加上限，下次触发磁流强化也会叠加此作用号减伤
  - 自身突击步兵受到伤害减少 XX.XX%
    - 该作用号为伤害减少效果，作用号自身叠加为作用值累加计算，与其他作用号累乘计算，即 
      - 最终伤害 = 基础伤害 *（1 + 各类加成）*（1 - 各类减免）*（1 - 【本作用值】* 敌方兵种修正系数/10000）
      - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect12617SoldierAdjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
  - （可叠加，最多提升 X 层）
    - 层数上限读取const表，字段effect12617Maxinum
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12617)
public class Checker12617 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (BattleConst.WarEff.MASS.check(parames.troopEffType) || parames.solider.getEffVal(EffType.HERO_12611) <= 0 || parames.tarStatic.getPlayerArmyCountMap().size() > 1) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		effPer = parames.unity.getEffVal(effType());
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
