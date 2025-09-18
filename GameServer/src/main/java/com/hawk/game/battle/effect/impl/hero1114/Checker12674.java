package com.hawk.game.battle.effect.impl.hero1114;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 【12674】
- 【万分比】【12674】协同攻击：每5（effect12674AtkRound）回合回合开始时，自身出征数量最多的攻城车单位对敌方所有单位发动 1（effect12674AtkTimes）轮【协同攻击】（伤害率 180.00%（作用号12674），命中后降低其攻击+500.00%（effect12674BaseVaule1），超能攻击+100.00%（effect12674BaseVaule2），持续 2 （effect12674ContinueRound）回合（至多叠加2（effect12674Maxinum）层）->针对敌方兵种留个内置系数effect12674SoldierAdjust
  - 若触发【协同攻击】时火力值和援护值均达到30，则在追加1轮【协同攻击】，清除所有被命中单位的【近战支援】（作用号【12001/12002/12003/12004/12021】）【远程支援】（作用号【12005/12006/12007/12008/12022】）效果（一场战斗只能触发一次）
  - 注：需满足12671攻城车数量限制，作用号才生效
  - 战报相关
  - 于战报中隐藏
  - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 协同攻击：每5（effect12674AtkRound）回合回合开始时，对敌方所有单位发动2（effect12674AtkTimes）轮【协同攻击】
    - 每第 X 回合
      - 战斗中每第 X 的倍数的回合开始后，自身开始普攻前，额外进行X次攻击（可以理解为释放X次技能效果）
        - 注：此攻击效果为额外攻击效果，无法触发英雄茉碧乌丝的专属芯片-燃烧火箭弹（作用号【1540】）的效果
        - 注：此效果于自身开始普攻前即可触发（即在攻城车无法普攻的回合时，也能触发此效果）
      - 指定回合数读取const表，字段effect1674AtkRound
        - 配置格式：绝对值
      - 攻击次数读取const表，字段effect12674AtkTimes
        - 配置格式：绝对值
  - （伤害率 180.00%（作用号12674）->针对敌方兵种留个内置系数effect12674SoldierAdjust
    - 即 实际伤害 =  基础伤害 *（1 + 各类加成）* 伤害率 * 敌方兵种修正系数/10000
      - 实际针对敌方各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect12674SoldierAdjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
  - 命中后降低其攻击+500.00%，超能攻击+100.00%
    - 降低敌方加成为外围属性减少效果
      - 即 敌方部队实际属性 = 基础属性*（1 + 敌方各类属性加成 - 属性降低*叠加次数）
      - 攻击降级读取const表，字段effect1674BaseVaule1
        - 配置格式：万分比
      - 超能攻击降低读取const表，字段effect12674BaseVaule2
        - 配置格式：万分比
      - 注：（1 + 敌方各类属性加成 - 属性降低*叠加次数）最小值 为 1
  - 持续 2 （effect12674ContinueRound）回合，（至多叠加2（effect12674Maxinum）层）
    - 持续回合数指定数值读取const表，字段effect12674ContinueRound
      - 配置格式：绝对值
    - 叠加层数指定数值读取const表，字段effect12674Maxinum
      - 配置格式：绝对值
    - 叠加时如有超过层数的buff，优先取作用号【12674】数值较高的
  - 若触发【协同攻击】时火力值和援护值均达到30(effect12674AtkThresholdValue)，则额外进行一次【协同攻击】，清除所有被命中单位的【近战支援】（作用号【12001/12002/12003/12004/12021】）【远程支援】（作用号【12005/12006/12007/12008/12022】）效果（一场战斗只能触发一次）
    - 火力值 >= 指定数值 且 支援值 >= 指定数值 时生效
      - 指定数值读取const表，字段effect12674AtkThresholdValue
        - 配置格式：绝对值
    - 实际火力值 = effect12335AddFirePoint*叠加次数 + effect12671AddFirePoint*叠加次数
    - 实际援护值 = effect12331AddEscortPoint*叠加次数 + effect12672AddEscortPoint*叠加次数
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12674)
public class Checker12674 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12671) <= 0) {
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
