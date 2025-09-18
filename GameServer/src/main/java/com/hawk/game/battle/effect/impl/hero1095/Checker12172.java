package com.hawk.game.battle.effect.impl.hero1095;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 【12172~12173】
- 【万分比】【12172~12173】集结战斗时，若自身出征采矿车数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的采矿车于本场战斗中获取如下效果（多个杰西卡同时存在时，至多有 2 个采矿车单位生效）：浴火：采矿车受到近战单位攻击时，伤害减少 【XX.XX% + 敌方燃烧单位数*XX.XX%】（敌方处于【燃烧状态】的单位计算时至多取 10 个）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 集结战斗开始前，若自身出征携带不低于集结部队总数 5% 的采矿车
    - 数量1 = 某玩家出征携带的采矿车（兵种类型 = 8）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12172AllNumLimit、effect12173AllNumLimit
        - 配置格式：万分比
  - 集结战斗开始前，若自身携带超过自身部队总数 50% 的采矿车
    - 数量1 = 某玩家出征携带的采矿车（兵种类型 = 8）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12172SelfNumLimit、effect12173SelfNumLimit
        - 配置格式：万分比
  - 自身出征数量最多的采矿车在本场战斗中获得如下效果
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的采矿车（兵种类型 = 8）生效（若存在多个采矿车数量一样且最高，取等级高的）
    - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队不生效
  - 浴火：采矿车受到近战单位攻击时，伤害减少 【XX.XX% + 敌方燃烧单位数*XX.XX%】（敌方处于【燃烧状态】的单位计算时至多取 10 个）
    - 采矿车（兵种类型 = 8）受到近战单位攻击时，伤害减少 【XX.XX% + 敌方燃烧单位数*XX.XX%】
      - 近战部队包含：主战坦克（兵种类型 = 2）、防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、轰炸机（兵种类型 = 3）
    - 该作用号为伤害减少效果，与其他作用号累乘计算，即 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【12172】 - 【12173】）
      - 【12173】实际数值 = 敌方当前处于【燃烧状态】的单位数*英雄系数
        - 敌方单位数：敌方战报中的单排计数
          - 不同玩家不同等级的部队（兵种类型 = 1~8）均视为独立单位
          - 单人时取敌方单人所有部队，集结时取集结敌方所有玩家的所有部队
        - 另外该计算对于敌方单位数量有上限限制，读取const表，字段effect12173MaxNum
          - 配置格式：绝对值
    - （多个杰西卡同时存在时，至多有 2 个采矿车单位获得上述效果）
      - 注：这里限制的是集结战斗中某方拥有此作用号效果的采矿车的数量；若集结中超出此上限，取数值高的；即这里只能有 2 个玩家携带的作用号生效
        - 层数上限读取const表，字段effect12172Maxinum、effect12173Maxinum
          - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12172)
public class Checker12172 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12163) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		return new CheckerKVResult(parames.unity.getEffVal(effType()), 0);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
