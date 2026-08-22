package com.hawk.game.battle.effect.impl.hero1116;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12723)
public class Checker12723 implements IChecker {
	/**
	 * 【12723~12726】
- 【万分比】【12723~12726】瞬闪换镜：诺菲娅在极短时间内切换狙击镜倍率，并同步刷新量子视觉的环境解析数据。常规形态为狙击形态，如果本回合光量子信标反馈目标中非步兵单位数量不少于2（effect12723NumLimit）个时，自动触发形态变化变为紧凑形态，否则切换回狙击形态
  - 狙击形态：视野转为冷色调红外成像，标记敌人热能轮廓，攻击能精准命中敌方弱点部位使其晕眩，自身攻击增加+XX.XX%【12723】，且受到攻击的敌方部队中XX.XX%【12724】数量的部队下一回合无法进行攻击(->针对敌方兵种留个内置系数effect12724SoldierAdjust)
  - 紧凑形态：自身超能攻击增加+XX.XX%【12725】，同步解析多个光反射角度，攻击时多道光束聚焦一点，造成穿透性灼伤效果，造成一次超能攻击伤害，伤害率+XX.XX%【12726】。(->针对敌方兵种留个内置系数effect12726SoldierAdjust)
  - 战报相关
  - 于战报中隐藏
  - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 瞬闪换镜：诺菲娅在极短时间内切换狙击镜倍率，并同步刷新量子视觉的环境解析数据。常规形态为狙击形态，如果本回合光量子信标反馈目标中非步兵单位数量不少于2（effect12723NumLimit）个时，自动触发形态变化变为紧凑形态，否则切换回狙击形态
    - 非步兵单位包含有：防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、主战坦克（兵种类型 = 2）、轰炸机（兵种类型 = 3）、直升机（兵种类型 = 4）、攻城车（兵种类型 = 7）
    - 步兵单位包含有：突击步兵（兵种类型 = 5）、狙击兵（兵种类型 = 6）
    - 光量子信标目标中（作用号【12722】）非步兵单位 ≥ 固定值 则切换至紧凑形态，否则切换至狙击形态
      - 固定值读取const表，effect12723NumLimit 字段
        - 配置格式：绝对值
      - 持续时间均为当前回合
  - 狙击形态：视野转为冷色调红外成像，标记敌人热能轮廓，攻击能精准命中敌方弱点部位使其晕眩，自身攻击增加+XX.XX【12723】%，且受到攻击的敌方部队中XX.XX%【12724】数量的部队下一回合无法进行攻击(->针对敌方兵种留个内置系数effect12724SoldierAdjust)
    - 自身攻击增加为常规外围属性加成效果，与其他作用号累加计算
      - 即 实际属性 = 基础属性*（1 + 各类攻击加成 +【12723作用值】）
        - 配置格式：万分比
    - 敌方部队无法进行攻击逻辑为：敌方部队下一回合伤害计算时，部队中兵的数量计算时减少即可
      - 即 实际攻击的兵的数量 = 原本数量 * （1 - A狙击【12724作用值】* 敌方兵种修正系数/10000-B狙击【12724作用值】* 敌方兵种修正系数/10000）
        - 配置格式：万分比
      - 实际针对敌方各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect12724SoldierAdjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
  - 紧凑形态：自身超能攻击增加+XX.XX%【12725】，同步解析多个光反射角度，攻击时多道光束聚焦一点，造成穿透性灼伤效果，造成一次超能攻击伤害，伤害率+XX.XX%【12726】。(->针对敌方兵种留个内置系数effect12726SoldierAdjust)
  - 自身超能攻击增加为常规外围属性加成效果，与其他作用号累加计算
    - 即 实际属性 = 基础属性*（1 + 各类攻击加成 +【12725作用值】）
      - 配置格式：万分比
  - 伤害率为常规伤害率效果
    - 即 实际伤害 = 基础伤害 *（1 + 各类加成）*【12756作用值】* 敌方兵种修正系数/10000
      - 配置格式：万分比
    - 实际针对敌方各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect12726SoldierAdjust
      - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
        - 修正系数具体配置为万分比
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12721) == 0) {
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
