package com.hawk.game.battle.effect.impl.hero1114;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.hawk.os.HawkException;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

/**
 *- 【12671~12672】
- 【万分比】【12671~12672】集结战斗开始前，若自身出征攻城车数量超过自身出征部队总数 50%(effect12671SelfNumLimit)且不低于集结部队总数 5%(effect12671AllNumLimit)，自身出征数量最多的攻城车于本场战斗中获取如下效果（多个阿斯缇娅同时存在时，至多有 2 (effect12671Maxinum)个攻城车单位生效）:
  - 火力联合：自身火力值上限增加10(effect12671PlusFirePoint)，援护值的上限增加10(effect12672PlusEscortPoint)
    - 自身每次进入奇数回合时立即增加火力值+2(effect12671AddFirePoint) ，火力值≥15 (effect12671AtkThresholdValue)时，自身攻城车造成伤害增加 +90.00%（作用号12671）->针对敌方兵种留个内置系数effect12671SoldierAdjust，并在攻击时无视目标部分生命加成（无视值 = 火力值 *5%(effect12671BaseVaule）
    - 自身每次进入偶数回合时立即增加援护值+2(effect12672AddEscortPoint) ，援护值≥15 (effect12672AtkThresholdValue)时，自身攻城车受到伤害减少 +30.00%（作用号12672）->针对敌方兵种留个内置系数effect12672SoldierAdjust，并在受击时无视目标部分攻击加成（无视值 = 援护值 *5%(effect12672BaseVaule）
  - 战报相关
  - 于战报中隐藏
  - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 集结战斗开始前，若自身出征攻城车车数量超过自身出征部队总数50%
    - 数量1 = 某玩家出征携带的攻城车（兵种类型 = 7）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12671SelfNumLimit
        - 配置格式：万分比
  - 且不低于集结部队总数 5%
    - 数量1 = 某玩家出征携带的攻城车（兵种类型 = 7）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12671AllNumLimit
        - 配置格式：万分比
  - 自身出征数量最多的攻城车在本场战斗中获得如下效果
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的攻城车（兵种类型 = 7）生效（若存在多个采矿车数量一样且最高，取等级高的）
    - 注：若存在荣耀所罗门的幻影部队，此作用号对自身幻影部队不生效
  - 火力联合：自身火力值上限增加10(effect12671PlusFirePoint)，援护值的上限增加10(effect12672PlusEscortPoint)
    - 火力值和援护值联动威尔森作用号【240627】【SSS】【军事】【攻城车】【威尔森】 【1100】，本次的增加值增为原火力值和援护值的扩充
      - 原火力值和上限
        - 增加火力值读取const表，字段effect12335AddFirePoint
          - 配置格式：绝对值
        - 另外火力值有数量上限，读取const表，字段effect12335MaxFirePoint
          - 配置格式：绝对值
      - 原援护值和上限
        - 增加援护值读取const表，字段effect12331AddEscortPoint
          - 配置格式：绝对值
        - 另外援护值有数量上限，读取const表，字段effect12331MaxEscortPoint
          - 配置格式：绝对值
    - 因此上限叠加
      - 实际火力值上限 = effect12335MaxFirePoint + effect12671PlusFirePoint
      - 实际援护值上限 = effect12331MaxEscortPoint + effect12672PlusEscortPoint
    - 额外火力上限读取const表，字段effect12671PlusFirePoint
      - 配置格式：绝对值
    - 额外援护上限读取const表，字段effect12671PlusEscortPoint
      - 配置格式：绝对值
  - 自身每次进入奇数回合时立即增加火力值+2(effect12671AddFirePoint) 
    - 火力值影响威尔森作用号【240627】【SSS】【军事】【攻城车】【威尔森】 【1100】，【12331~12334】
    - 援护值 = 威尔森提供火力值（effect12335AddFirePoint*12335触发次数）+ 阿斯缇娅提供的火力值（ effect12671AddFirePoint*12671触发次数 ）
      - 注：原只有威尔森12335提供火力值
      - 指定数值读取const表，字段effect12671AddFirePoint
        - 配置格式：绝对值
  - 火力值≥15 (effect12671AtkThresholdValue)时，自身攻城车造成伤害增加 +90.00%（作用号12671）->针对敌方兵种留个内置系数effect12671SoldierAdjust
    - 火力值 >= 指定数值时生效
      - 指定数值读取const表，字段effect12671AtkThresholdValue
        - 配置格式：万分比
    - 作用号为外围伤害加成效果，与其他作用号累加，即 
      - 实际伤害 = 基础伤害*（1 + 各类加成）*（1 - 各类减免）* （1+【本作用值】* 敌方兵种修正系数/10000）
        - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect12671SoldierAdjust
          - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
            - 修正系数具体配置为万分比
  - 并在攻击时无视目标部分生命加成（无视值 = 火力值 *2%(effect12671BaseVaule）
    - 效果为结算伤害时，减掉部分敌方生命加成
      - 敌方本次生命加成 = 敌方原本生命加成（万分比） - 无视值（万分比）
        - 指定数值读取const表，字段effect12671BaseVaule
        - 配置格式：万分比
        - 注：无视值不能大于敌方生命加成
  - 自身每次进入偶数回合时立即增加援护值+2(effect12672AddEscortPoint) 
    - 援护值影响威尔森作用号【240627】【SSS】【军事】【攻城车】【威尔森】 【1100】，【12335~12338】
    - 援护值 = 威尔森提供援护值（effect12331AddEscortPoint*12331触发次数）+ 阿斯缇娅提供的援护值（ effect12672AddEscortPoint*12672触发次数）
      - 注：原只有威尔森12331提供援护值
    - 指定数值读取const表，字段effect12672AddEscortPoint
      - 配置格式：绝对值
  - 援护值≥15 (effect12672AtkThresholdValue)时，自身攻城车受到伤害减伤 +30.00%（作用号12672）->针对敌方兵种留个内置系数effect12672SoldierAdjust
    - 援护值 >= 指定数值时生效
      - 指定数值读取const表，字段effect12672AtkThresholdValue
        - 配置格式：万分比
    - 该作用号为伤害减少效果，与其他作用号累乘计算，即 
      - 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【本作用值】* 敌方兵种修正系数/10000 * 叠加数）
        - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect12672SoldierAdjust
          - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
            - 修正系数具体配置为万分比
  - 并在受击时无视目标部分攻击加成（无视值 = 援护值 *2%(effect12671BaseVaule）
    - 效果为结算伤害时，减掉部分敌方攻击加成
      - 敌方本次攻击加成 = 敌方原本攻击加成（万分比） - 无视值（万分比）
        - 指定数值读取const表，字段effect12672BaseVaule
        - 配置格式：万分比
        - 注：无视值不能大于敌方攻击加成
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12671)
public class Checker12671 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {

		if (parames.type != SoldierType.CANNON_SOLDIER_7 || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}
		
		if (parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.CANNON_SOLDIER_7)) {
			return CheckerKVResult.DefaultVal;
		}

		Map<String, Integer> effPlayerVal = (Map<String, Integer>) parames.getLeaderExtryParam(getSimpleName());
		if (effPlayerVal == null) {
			effPlayerVal = selectPlayer(parames);
			parames.putLeaderExtryParam(getSimpleName(), effPlayerVal);
		}

		if (!effPlayerVal.containsKey(parames.unity.getPlayerId())) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = effPlayerVal.get(parames.unity.getPlayerId());
		return new CheckerKVResult(effPer, 0);
	}

	/**数值最高的玩家*/
	private Map<String, Integer> selectPlayer(CheckerParames parames) {
		Map<String, Integer> valMap = new LinkedHashMap<>();
		for (BattleUnity unity : parames.unityList) {
			if (valMap.containsKey(unity.getPlayer())) {
				continue;
			}
			int effvalue = effvalue(unity, parames);
			valMap.put(unity.getPlayerId(), effvalue);
		}

		valMap = valMap.entrySet().stream()
				.sorted(((item1, item2) -> {
					int compare = item2.getValue().compareTo(item1.getValue());
					return compare;
				}))
				.filter(ent -> ent.getValue() > 0)
				.limit(ConstProperty.getInstance().effect12671Maxinum)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return valMap;
	}

	private int effvalue(BattleUnity unity, CheckerParames parames) {
		try {
			if (unity.getEffVal(effType()) == 0) {
				return 0;
			}
			String playerId = unity.getPlayerId();
			int effPer = 0;
			// 采矿车数量
			int march8cnt = parames.unitStatic.getPlayerSoldierCountMarch().get(playerId, SoldierType.CANNON_SOLDIER_7);
			// 若自身出征携带不低于集结部队总数 5% 的采矿车
			if (march8cnt / parames.unitStatic.getTotalCountMarch() >= ConstProperty.getInstance().effect12671AllNumLimit * GsConst.EFF_PER) {
				// 若自身携带超过自身部队总数 50% 的采矿车
				if (march8cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > ConstProperty.getInstance().effect12671SelfNumLimit * GsConst.EFF_PER) {
					effPer = unity.getEffVal(effType());
				}
			}
			return effPer;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
