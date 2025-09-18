package com.hawk.game.battle.effect.impl.hero1112;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * - 磁锋力场：战斗开始时，使自身突击步兵部署磁力装置，于战场中形成磁锋力场，自身出征数量最多的突击步兵获得下述效果：
- 【万分比】【12616】感应防域：每X(effect12616AtkRound) 回合开始时展开感应防域，持续X(effect12616ContinueRound) 回合，防域存在期间自身受到伤害减少+XX.XX%【12616】，且每次受到伤害时，对伤害来源做出应对措施，受到伤害来源同兵种的伤害额外减少XX.XX%(effect12616BaseVaule)（该效果可叠加，每回合每个敌方兵种至多触发X（effect12616MaxTimes）次，至多X（effect12616Maxinum）层）->针对所有敌方兵种留个内置系数
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 自身出征数量最多的突击步兵获得下述效果
    - 仅自身出征数量最多的突击步兵获得效果，突击步兵（兵种类型=5）
  - 每第 X 回合开始时展开感应防域
    - 战斗中每第 X 的倍数的回合开始后，自身开始普攻前，进行判定
    - 指定回合数读取const表，字段effect12616AtkRound
      - 配置格式：绝对值
  - 持续X回合
    - 回合开始：进入状态的回合，当前回合算作第1回合
    - 回合结束：当前回合结束时进行判定，如果 当前持续回合=理应持续回合，则清除状态
    - 持续回合数读取const表，字段effect12616ContinueRound
      - 配置格式：绝对值
  - 防域存在期间自身受到伤害减少+XX.XX%【12616】
    - 该作用号为伤害减少效果，作用号自身叠加为作用值累加计算，与其他作用号累乘计算，即 
      - 最终伤害 = 基础伤害 *（1 + 各类加成）*（1 - 各类减免）*（1 - 【本作用值】* 敌方兵种修正系数/10000）
      - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect12616SoldierAdjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
  - 且每次受到伤害时，对伤害来源做出应对措施，受到伤害来源同兵种的伤害额外减少XX.XX%
    - 伤害来源同兵种 指代 8个兵种
      - 防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、主战坦克（兵种类型 = 2）、轰炸机（兵种类型 = 3）、狙击兵（兵种类型 = 6）、突击步兵（兵种类型 = 5）、直升机（兵种类型 = 4）、攻城车（兵种类型 = 7）
    - 最终伤害 = 基础伤害 *（1 + 各类加成）*（1 - 各类减免）*（1 - （【本作用值】+固定值*叠加次数）* 敌方兵种修正系数/10000）
      - 增加值读取const表，字段effect12616BaseVaule
        - 配置格式：万分比
  - （该效果可叠加，每回合每个敌方兵种至多触发X次，至多X层）
    - 每回合触发上限读取const表，字段effect12616MaxTimes
      - 配置格式：绝对值
    - 层数上限读取const表，字段effect12616Maxinum
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12616)
public class Checker12616 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity.getEffVal(EffType.HERO_12611) <= 0) {
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
