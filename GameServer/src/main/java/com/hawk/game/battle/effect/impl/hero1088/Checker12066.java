package com.hawk.game.battle.effect.impl.hero1088;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hawk.os.HawkException;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.BattleSoldier_3;
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
【12066】
- 【万分比】【12066】集结战斗开始前，若自身出征直升机数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的直升机单位获得如下效果: 【协同轰炸】: 
与拥有【轮番轰炸】技能的出征数量最多的友军轰炸机部队进行联合行动，使其在释放【轮番轰炸】技能时，可攻击轮次 +XX，伤害额外 +XX.XX%（多个新英雄同时存在时，至多有 2 个直升机单位生效）
- 该作用号为作用号【12051】触发后的额外效果
- 战报相关
  - 于战报中隐藏
  - 不合并至精简战报中
- 仅对集结战斗生效（包含集结进攻和集结防守）
- 在战斗开始前判定，满足条件后本场战斗全程生效
- 集结战斗开始前，若自身出征直升机（兵种类型 = 4）数量超过不低于集结部队总数 5%
  - 数量1 = 某玩家出征携带的直升机（兵种类型 = 4）数量
    - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
  - 数量2 = 集结部队的出征数量总和
    - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
  - 数量1/数量2 >= 指定数值时生效
    - 指定数值读取const表，字段effect12066AllNumLimit
      - 配置格式：万分比
- 集结战斗开始前，若自身出征直升机（兵种类型 = 4）数量超过自身出征部队总数 50%
  - 数量1 = 某玩家出征携带的直升机（兵种类型 = 4）数量
    - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
  - 数量2 = 某玩家出征数量
    - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
  - 数量1/数量2 > 指定数量时生效
    - 指定数值读取const表，字段effect12066SelfNumLimit
      - 配置格式：万分比
- 自身出征数量最多的直升机单位在本场战斗中获得如下效果
  - 满足条件后，该作用号效果仅对玩家出征时数量最多的直升机（兵种类型 = 4）生效（若存在多个直升机数量一样且最高，取等级高的）
- 【协同轰炸】: 与拥有【轮番轰炸】技能的出征数量最多的友军轰炸机部队进行联合行动，使其在释放【轮番轰炸】技能时，可攻击轮次 +XX，伤害额外 +XX.XX%
  - 该作用号为作用号【12051】触发后的额外效果
  - 绑定在出征数量最多的我方友军轰炸机（兵种类型 = 3）部队上；选择机制如下
    - 先筛选出拥有【12051】的我方友军轰炸机（若不存在，则此作用号实际无效）
    - 按出征数量选择其中数量最多的轰炸机单位（若存在多个数量最多的轰炸机单位，取战报排序靠前的）
  - 注：若绑定的友军轰炸机单位存在荣耀所罗门的幻影部队，此作用号对幻影部队也生效
  - 此伤害加成为额外伤害加成，与其他各类伤害加成累加计算；即实际伤害 = 基础伤害*（1 + 各类伤害加成 + 【本作用值】）
  - 额外可攻击轮次读取const表，字段effect12066AtkTimes
    - 即绑定后的轰炸机单位发动【轮番轰炸】【id = 12051】技能时，实际可攻击轮次 = 基础轮次 + 【effect12066AtkTimes】
    - 配置格式：绝对值
- （多个新英雄同时存在时，至多有 2 个直升机单位生效）
  - 若集结战斗中己方存在多个此作用号效果，限制其生效个数上限（若超出此上限，取作用号数值高的，若作用号数值相同，则取战报列表中排序靠前的）；即这里只能有 2 个玩家携带的作用号生效
    - 层数上限读取const表，字段effect12066Maxinum
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.EFF_12066)
public class Checker12066 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.type != SoldierType.PLANE_SOLDIER_4 || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}

		String playerId = parames.unity.getPlayerId();
		BattleUnity maxUnity = parames.getPlayerMaxFreeArmy(playerId, SoldierType.PLANE_SOLDIER_4);
		if (maxUnity != parames.unity) {
			return CheckerKVResult.DefaultVal;
		}

		// 属性最高的二个人
		Map<String, Integer> effPlayerVal;
		Object object = parames.getLeaderExtryParam(getSimpleName());
		if (Objects.nonNull(object)) {
			effPlayerVal = (Map<String, Integer>) object;
		} else {
			effPlayerVal = selectPlayer(parames);
			parames.putLeaderExtryParam(getSimpleName(), effPlayerVal);
		}

		if (effPlayerVal.isEmpty()) {
			return CheckerKVResult.DefaultVal;
		}

		if (!effPlayerVal.containsKey(playerId)) {
			return CheckerKVResult.DefaultVal;
		}

		BattleSoldier_3 max3 = (BattleSoldier_3) parames.unitStatic.getUnityList().stream()
				.filter(u -> u.getSolider().getType() == SoldierType.PLANE_SOLDIER_3)
				.map(BattleUnity::getSolider)
				.filter(s -> s.getEffVal(EffType.EFF_12051) > 0)
				.sorted(Comparator.comparingInt(BattleSoldier::getFreeCnt).thenComparingInt(BattleSoldier::getLevel).reversed())
				.findFirst().orElse(null);
		int effPer = parames.unity.getEffVal(effType());
		if (Objects.nonNull(max3)) {
			parames.unity.getSolider().addDebugLog("{} + {}  12066 {}", parames.unity.getSolider().getUUID(), max3.getUUID(), effPer);
			max3.addEff12066Val(effPer);
			max3.addEffect12066AtkTimes(ConstProperty.getInstance().getEffect12066AtkTimes());
		}

		return new CheckerKVResult(effPer, 0);
	}

	/**数值最高的玩家*/
	private Map<String, Integer> selectPlayer(CheckerParames parames) {
		Map<String, Integer> valMap = new HashMap<>();
		for (BattleUnity unity : parames.unityList) {
			if (valMap.containsKey(unity.getPlayerName())) {
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
				.limit(ConstProperty.getInstance().getEffect12062Maxinum())
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
			int march8cnt = parames.unitStatic.getPlayerSoldierCountMarch().get(playerId, SoldierType.PLANE_SOLDIER_4);
			// 若自身出征携带不低于集结部队总数 5% 的采矿车
			if (march8cnt / parames.unitStatic.getTotalCountMarch() >= ConstProperty.getInstance().getEffect12066AllNumLimit() * GsConst.EFF_PER) {
				// 若自身携带超过自身部队总数 50% 的采矿车
				if (march8cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > ConstProperty.getInstance().getEffect12066SelfNumLimit() * GsConst.EFF_PER) {
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
