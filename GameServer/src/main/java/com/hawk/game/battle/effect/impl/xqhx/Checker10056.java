package com.hawk.game.battle.effect.impl.xqhx;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.module.lianmengXianquhx.XQHXConst.XQHXState;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;

/*
 * 【10053】
【万分比】【10053】于先驱回响的核心建筑内战斗时，部队基础防御增加XX.XX%
- 生效逻辑和配置格式同【10052】，属性加成改为增加基础防御即可
- 兵种修正系数 读取const表，字段effect10053SoldierAdjust
【10054】
【万分比】【10054】于先驱回响的核心建筑内战斗时，部队基础生命增加XX.XX%
- 生效逻辑和配置格式同【10052】，属性加成改为增加基础生命即可
- 兵种修正系数 读取const表，字段effect10054SoldierAdjust
【10055】
【万分比】【10055】于先驱回响的核心建筑内战斗时，部队攻击、防御、血量增加XX.XX%
- 生效逻辑和配置格式同【10052】
  - 该作用号为常规外围属性加成效果，与其他作用号累加计算
    - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】* 自身兵种修正系数/10000）
      - 实际针对敌方各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect10055SoldierAdjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
【10056】
【万分比】【10056】于先驱回响的核心建筑内战斗时，部队超能攻击增加XX.XX%
- 生效逻辑和配置格式同【10052】
  - 该作用号为常规外围属性加成效果，与其他作用号累加计算
    - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】* 自身兵种修正系数/10000）
      - 实际针对敌方各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect10056SoldierAdjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
【10057】
【万分比】【10057】于先驱回响的核心建筑内战斗时，部队造成伤害增加XX.XX%
  - 该作用号为外围伤害加成效果，与其他作用号累乘
    - 即 实际伤害 = 基础伤害*（1 + 各类加成）*（1 - 各类减免）* （1+【本作用值】* 自身兵种修正系数/10000）
      - 实际针对敌方各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect10057SoldierAdjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
【10058】
【万分比】【10058】部队攻击、防御、血量增加XX.XX%
- 该作用号为常规外围属性加成效果，与其他作用号累加计算
  - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】* 自身兵种修正系数/10000）
    - 实际针对敌方各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect10058SoldierAdjust
      - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
        - 修正系数具体配置为万分比
【10059】
【万分比】【10059】部队超能攻击增加XX.XX%
- 该作用号为常规外围属性加成效果，与其他作用号累加计算
  - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】* 自身兵种修正系数/10000）
    - 实际针对敌方各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect10059SoldierAdjust
      - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
        - 修正系数具体配置为万分比
【10060】
【万分比】【10060】部队造成伤害增加XX.XX%
- 该作用号为外围伤害加成效果，与其他作用号累乘
  - 即 实际伤害 = 基础伤害*（1 + 各类加成）*（1 - 各类减免）* （1+【本作用值】* 自身兵种修正系数/10000）
    - 实际针对敌方各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect10060SoldierAdjust
      - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
        - 修正系数具体配置为万分比
  - 注：此前部队伤害增加作用号12101中是没有轰炸机伤害加成效果的，此处注意作用号11060是需要有轰炸机伤害加成效果的
 */
@BattleTupleType(tuple = { Type.ATKFIRE })
@EffectChecker(effType = EffType.XQHX_10056)
public class Checker10056 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.unity.getPlayer().getXQHXState() == XQHXState.GAMEING && isSoldier(parames.type)) {
			effPer = (int) (parames.unity.getEffVal(effType()) * GsConst.EFF_PER* ConstProperty.getInstance().effect10056SoldierAdjustMap.getOrDefault(parames.type, 10000));
		}

		return new CheckerKVResult(effPer, effNum);
	}
	
	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
