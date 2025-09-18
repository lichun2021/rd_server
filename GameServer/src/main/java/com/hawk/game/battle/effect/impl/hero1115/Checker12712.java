package com.hawk.game.battle.effect.impl.hero1115;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;

/**
 *【12712】
- 【万分比】【12712】任命在战备参谋部时，集结战斗中对敌方非空军单位部署蜂群无人机，自身部队对被【蜂群干扰】的敌方单位进行攻击时，有20%（effect12712BaseVaule）的概率激活蜂群，使自身本次攻击时超能攻击增加+ XX.XX%（12712）->针对己方兵种留个内置系数（effect12712SoldierAdjust）
  - 战报相关
    - 战报中展示
    - 不合并至精简战报中
    - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 集结战斗中对敌方非空军单位部署蜂群无人机
    - 非空军部队类型包含有：防御坦克（兵种类型 = 1）、主战坦克（兵种类型 = 2）、突击步兵（兵种类型 = 5）、狙击兵（兵种类型 = 6）、攻城车（兵种类型 = 7）、采矿车（兵种类型 = 8）
  - 自身部队对被【蜂群干扰】的敌方单位进行攻击时，有20%的概率激活蜂群，使自身本次超能攻击增加+ XX.XX%
    - 该作用号为常规外围属性加成效果，与其他作用号累加计算
      - 概率为固定数值，读取const表，字段effect12712BaseVaule
      - 配置格式：万分比
      - 注：此处不作为概率触发，而是直接将 作用值*概率 作为 实际增加超能攻击（避免随机性）
        - 如 10%概率 增加300%超能攻击 直接生效为 每次都增加30%超能攻击
        - 即 实际属性 = 基础属性（1 + 各类加成 +【本作用值】 effect12712BaseVaule * 自身兵种修正系数）
      - 注：任何伤害均生效，且下个英雄会联动该作用号将概率提高并附加额外效果，可以预留开发位置
  - ->针对自身兵种留个内置系数
    - 实际针对自身各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect12712SoldierAdjust
      - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
        - 修正系数具体配置为万分比
    - 配置格式：万分比
 */
@BattleTupleType(tuple = { Type.ATKFIRE })
@EffectChecker(effType = EffType.EFF_12712)
public class Checker12712 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}
		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type) && !isPlan(parames.tarType)) {
			effPer = (int) (parames.unity.getEffVal(effType()) * GsConst.EFF_PER * ConstProperty.getInstance().effect12712BaseVaule * GsConst.EFF_PER
					* ConstProperty.getInstance().effect12712SoldierAdjustMap.getOrDefault(parames.type, 0));
		}
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
