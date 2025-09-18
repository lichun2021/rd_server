package com.hawk.game.battle.effect.impl.ailinna12081;

import java.util.HashMap;
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
 * 【12084】
- 【万分比】【12084】近战鼓舞：集结战前，若自身出征防御坦克数量超过自身出征部队总数 50%且不低于集结部队总数 5%，使自身出征数量最多的防御坦克受到攻击时，伤害减少 +XX.XX%（多个埃琳娜存在时，至多有 2 个防御坦克生效）
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 该作用号仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 若自身出征防御坦克数量超过自身出征部队总数 50%
    - 数量1 = 某玩家出征携带的防御坦克（兵种类型 = 1）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12084SelfNumLimit
        - 配置格式：万分比
  - 若自身出征携带防御坦克数量不低于集结部队总数 5%
    - 数量1 = 某玩家出征携带的防御坦克（兵种类型 = 1）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12084AllNumLimit
        - 配置格式：万分比
  - 使自身出征数量最多的防御坦克受到攻击时，伤害减少 +XX.XX%（多个埃琳娜存在时，至多有 2 个防御坦克生效）
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的防御坦克（兵种类型 = 1）生效（若存在多个防御坦克数量一样且最高，取等级高的）（战中不改变目标）
    - 此伤害减少效果算式与其他伤害减少效果累乘计算；即实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 己方某伤害减少）*（1 - 本作用值伤害减少）
    - 单玩家拥有此作用号时，只能提供 1 层效果；集结中存在多个此作用号时，可以同时生效
    - 效果记录在己方部队身上；限制该作用号生效的玩家数量
      - 注：这里限制的是生效玩家数量，若集结中拥有此作用号的玩家数量超出此上限，取作用号数值高的生效
      - 层数上限读取const表，字段effect12084Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.EFF_12084)
public class Checker12084 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!BattleConst.WarEff.MASS.check(parames.troopEffType) || parames.type != SoldierType.TANK_SOLDIER_1) {
			return CheckerKVResult.DefaultVal;
		}

		if (parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.TANK_SOLDIER_1)) {
			return CheckerKVResult.DefaultVal;
		}

		Map<String, Integer> effPlayerVal = selectPlayer(parames);
		if (!effPlayerVal.containsKey(parames.unity.getPlayerId())) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = effPlayerVal.get(parames.unity.getPlayerId());
		parames.addDebugLog("-【12084】近战鼓舞：集结战前，若自身出征防御坦克 {} 获得 12084 {}", parames.unity.getSolider().getUUID(), effPer);
		return new CheckerKVResult(effPer, 0);
	}

	/**数值最高的玩家*/
	private Map<String, Integer> selectPlayer(CheckerParames parames) {
		Map<String, Integer> valMap = new HashMap<>();
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
				.limit(ConstProperty.getInstance().getEffect12084Maxinum())
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
			int march8cnt = parames.unitStatic.getPlayerSoldierCountMarch().get(playerId, SoldierType.TANK_SOLDIER_1);
			// 若自身出征携带不低于集结部队总数 5% 的采矿车
			if (march8cnt / parames.unitStatic.getTotalCountMarch() >= ConstProperty.getInstance().getEffect12084AllNumLimit() * GsConst.EFF_PER) {
				// 若自身携带超过自身部队总数 50% 的采矿车
				if (march8cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > ConstProperty.getInstance().getEffect12084SelfNumLimit() * GsConst.EFF_PER) {
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
