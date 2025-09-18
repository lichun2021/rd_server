package com.hawk.game.battle.effect.impl.xqhx;

import com.hawk.game.battle.effect.BattleConst;
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
 * 【万分比】【10061】集结战斗攻击增加XX.XX%
【万分比】【10062】集结战斗防御增加XX.XX%
【万分比】【10063】集结战斗生命增加XX.XX%
【万分比】【10064】集结战斗造成伤害增加XX.XX%
【万分比】【10065】集结战斗受到伤害增加XX.XX%
- 注：以上5个作用号仅集结战斗中才生效，且仅在先驱回响副本内生效
- 战报相关
  - 于战报中显示
  - 合并至精简战报中
- 攻击增加XX.XX%【10061】
  - 该作用号为常规外围属性加成效果，与其他作用号累加计算
    - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】* 自身兵种修正系数/10000）
      - 实际针对敌方各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect10061SoldierAdjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
- 防御增加XX.XX%【10062】
  - 该作用号为常规外围属性加成效果，与其他作用号累加计算
    - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】* 自身兵种修正系数/10000）
      - 实际针对敌方各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect10062SoldierAdjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
- 生命增加XX.XX%【10063】
  - 该作用号为常规外围属性加成效果，与其他作用号累加计算
    - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】* 自身兵种修正系数/10000）
      - 实际针对敌方各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect10063SoldierAdjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
- 造成伤害增加XX.XX%【10064】
  - 该作用号为外围伤害加成效果，与其他作用号累乘
    - 即 实际伤害 = 基础伤害*（1 + 各类加成）*（1 - 各类减免）* （1+【本作用值】* 自身兵种修正系数/10000）
      - 实际针对敌方各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect10064SoldierAdjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
- 受到伤害减少 XX.XX%【10065】
  - 该作用号为伤害减少效果，与其他作用号累乘计算，即 
    - 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【本作用值】* 敌方兵种修正系数/10000 * 叠加数）
      - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect10065SoldierAdjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
 */
@BattleTupleType(tuple = { Type.DEF })
@EffectChecker(effType = EffType.XQHX_10067)
public class Checker10067 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.unity.getPlayer().getXQHXState() == XQHXState.GAMEING && parames.unitStatic.isNotMass() && parames.tarStatic.isNotMass() && isSoldier(parames.type)) {
			effPer = (int) (parames.unity.getEffVal(effType())* GsConst.EFF_PER * ConstProperty.getInstance().effect10067SoldierAdjustMap.getOrDefault(parames.type, 10000));
		}

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
