package com.hawk.game.battle.effect.impl.hero1109;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
 * 【12554】
- 【万分比】【12554】任命在战备参谋部时，集结战斗开始前，若集结全队出征采矿车数量不低于集结部队总数 10%(effect12554AllNumLimit)，自身出征数量最多的采矿车单位在命中近战单位时，
降低目标攻击、防御、生命+XX.XX%（12554）+固定值（effect12554BaseVaule）*战备参谋部中任命艾拉的友方成员数量（至多 2 (effect12554Maxinum) 个采矿车单位生效，该效果可叠加，至多XX%(effect12554MaxValue)，己方艾拉计数时至多取 7  (effect12554CountMaxinum)个）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 集结战斗开始前，若集结全队出征采矿车数量不低于集结部队总数 10%
    - 数量1 = 集结全队出征携带的采矿车数量：采矿车（兵种类型 = 8）
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12554AllNumLimit
        - 配置格式：万分比
  - 自身出征数量最多的采矿车单位在命中近战单位时，降低目标攻击、防御、生命+XX.XX%+固定值*战备参谋部中任命艾拉的友方成员数量
    - 近战部队包含：主战坦克（兵种类型 = 2）、防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、轰炸机（兵种类型 = 3）
    - 降低敌方加成为外围属性减少效果；即 敌方部队实际攻击/防御/生命 = 基础属性*（1 + 敌方各类属性加成 - 本作用号属性加成降低），同作用号【12063~12065】
    - 该效果由被攻击附加后，持续至本场战斗结束
    - 固定值读取const表，字段effect12554BaseVaule
      - 配置格式：万分比
  - （至多 2 个采矿车单位生效，该效果可叠加，至多XX%)
    - 集结中存在多个此作用号时，可以叠加
    - 集结己方生效此作用号的单位有数量限制，超出数量限制则取作用号数值高的单位
      - 数值上限读取const表，字段effect12554Maxinum
        - 配置格式：绝对值
    - 效果记录在敌方部队身上，数值直接叠加，即
      - 本作用号属性加成降低 = 采矿车A造成累计属性降低 + 采矿车B造成累计属性降低 
      - 限制该作用号叠加数值上限
        - 数值上限读取const表，字段effect12554MaxValue
          - 配置格式：万分比
  - （己方艾拉计数时至多取 7  个）
    - 取集结己方委任英雄中携带艾拉（ID1109）的玩家数量
    - 该计数有最高值限制，读取const表，字段effect12554CountMaxinum
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = { Type.SOLDIER_SKILL })
@EffectChecker(effType = EffType.EFF_12554)
public class Checker12554 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.CANNON_SOLDIER_8) || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}

		// 属性最高的二个人
		Integer effPer;
		Object object = parames.getLeaderExtryParam(getSimpleName());
		if (Objects.nonNull(object)) {
			effPer = ((Map<String, Integer>) object).getOrDefault(parames.unity.getPlayerId(), 0);
		} else {
			Map<String, Integer> selectPlayer = selectPlayer(parames);
			effPer = selectPlayer.getOrDefault(parames.unity.getPlayerId(), 0);
			parames.putLeaderExtryParam(getSimpleName(), selectPlayer);
		}

		if (effPer > 0) {
			parames.solider.addDebugLog("{} 在命中远程单位时，降低目标攻击、防御、生命 {}", parames.solider.getUUID(), effPer);
		}
		return new CheckerKVResult(effPer, 0);

	}

	/**数值最高的玩家*/
	private Map<String, Integer> selectPlayer(CheckerParames parames) {
		double total = parames.unitStatic.getTotalCountMarch();
		double tankCnt = parames.unitStatic.getArmyCountMapMarch().get(SoldierType.CANNON_SOLDIER_8);
		if (tankCnt / total < ConstProperty.getInstance().effect12554AllNumLimit * GsConst.EFF_PER) {
			return new HashMap<>();
		}

		// 战备参谋部中任命艾拉的友方成员数量
		int effect12554CountMaxinum = parames.unityList.stream().filter(unity -> unity.getEffVal(effType()) > 0).map(unity -> unity.getPlayerId()).collect(Collectors.toSet())
				.size();
		effect12554CountMaxinum = Math.min(effect12554CountMaxinum, ConstProperty.getInstance().effect12554CountMaxinum);

		Map<String, Integer> valMap = new HashMap<>();
		for (BattleUnity unity : parames.unityList) {
			if (valMap.containsKey(unity.getPlayerId())) {
				continue;
			}
			if (unity.getEffVal(effType()) > 0) {
				int effvalue = ConstProperty.getInstance().effect12554BaseVaule * effect12554CountMaxinum + unity.getEffVal(effType());
				valMap.put(unity.getPlayerId(), effvalue);
			}
		}

		Map<String, Integer> val = valMap.entrySet().stream()
				.sorted(((item1, item2) -> {
					int compare = item2.getValue().compareTo(item1.getValue());
					return compare;
				}))
				.filter(ent -> ent.getValue() > 0)
				.limit(ConstProperty.getInstance().effect12554Maxinum)
				.collect(Collectors.toMap(ent -> ent.getKey(), ent -> ent.getValue()));

		return val;
	}

}
