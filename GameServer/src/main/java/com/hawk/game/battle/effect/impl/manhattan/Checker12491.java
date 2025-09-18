package com.hawk.game.battle.effect.impl.manhattan;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * - 【万分比】【12491】基地防守战斗时，自身所有单位首回合受到伤害减少 +XX.XX%（此后每回合逐步衰减初始值的1/10；此效果在受到敌方空军攻击时 翻倍）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 该作用号仅在防守且防守自身基地或盟友基地时生效
    - 援助盟友时，援军方和被攻击的盟友均可生效，相互独立
  - 自身所有战斗单位是指所有参战的兵种（即兵种id = 1~8）
  - 此效果对战斗开始时（算作0回合？）造成的伤害也生效（数值算第1回合的）；如【11041】
[图片]
  - （此后每回合逐步衰减初始值的1/10；此效果在受到敌方空军攻击时 翻倍）
    - 该作用号为伤害减少效果，与其他作用号累乘计算，即 
      - 最终伤害 = 基础伤害 *（1 + 各类加成）*（1 - 各类减免）*（1 - 【衰减修正值】/10000）*敌方兵种修正系数/10000
    - 衰减修正值 = 面板数值 * 回合衰减系数/10000
      - 回合衰减系数 读取const表，字段effect12491RoundAdjust
        - 配置格式：回合数下限_回合数上限_回合衰减系数,回合数下限_回合数上限_回合衰减系数......
          - 回合数下限_回合数上限 为左右闭区间
          - 衰减系数具体配置为万分比
    - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect12491SoldierAdjust
      - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
        - 修正系数具体配置为万分比
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12491)
public class Checker12491 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (BattleConst.WarEff.CITY_DEF.check(parames.troopEffType)) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
