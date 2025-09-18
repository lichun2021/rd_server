package com.hawk.game.battle.effect.impl.ssstalent;

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
 * 12302】
- 【万分比】【12302】战技持续期间，若自身出征采矿车数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的采矿车每受到 1 次攻击后，将为己方随机 5 个近战单位提供 XX.XX% 的生命加成（该效果可叠加，至多 100 层；多个杰西卡同时存在时，至多有 2 个采矿车单位生效）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 此作用号仅在杰西卡开启战技后，战技持续期间才生效
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 集结战斗开始前，若自身出征携带不低于集结部队总数 5% 的采矿车
    - 数量1 = 某玩家出征携带的采矿车（兵种类型 = 8）数量
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12302AllNumLimit
        - 配置格式：万分比
  - 集结战斗开始前，若自身携带超过自身部队总数 50% 的采矿车
    - 数量1 = 某玩家出征携带的采矿车（兵种类型 = 8）数量
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12302SelfNumLimit
        - 配置格式：万分比
  - 自身出征数量最多的采矿车每受到 1 次攻击后
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的采矿车（兵种类型 = 8）生效（若存在多个采矿车数量一样且最高，取等级高的）（战中不再变化；若在战斗中阵亡，则此作用号失效）
  - 将为己方随机 5 个近战单位提供 XX.XX% 的生命加成
    - 纯随机 5 个近战单位（包含自身），不足数量则有多少取多少
    - 取单位数量读取const表，字段effect12302AffectNum
      - 配置格式：绝对值
    - 此生命加成效果为常规外围属性加成；即
      - 实际生命 = 基础生命*（1 + 各类加成效果 + 【本作用值】）
  - （该效果可叠加，至多 100 层；多个杰西卡同时存在时，至多有 2 个采矿车单位生效）
    - 该效果可叠加，叠加层数记录在受益的己方部队身上；层数上限读取const表，字段effect12302MaxTimes
      - 配置格式：绝对值
    - 注：这里额外限制集结战斗中某方拥有此作用号效果的采矿车的数量；若集结中超出此上限，取数值高的；即这里只能有 2 个玩家携带的作用号生效
      - 层数上限读取const表，字段effect12302Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12302)
public class Checker12302 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity.getEffVal(EffType.HERO_12181) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		if (parames.type != SoldierType.CANNON_SOLDIER_8 || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}
		
		if (parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.CANNON_SOLDIER_8)) {
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
				.limit(ConstProperty.getInstance().getEffect12302Maxinum())
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
			int march8cnt = parames.unitStatic.getPlayerSoldierCountMarch().get(playerId, SoldierType.CANNON_SOLDIER_8);
			// 若自身出征携带不低于集结部队总数 5% 的采矿车
			if (march8cnt / parames.unitStatic.getTotalCountMarch() >= ConstProperty.getInstance().getEffect12302AllNumLimit() * GsConst.EFF_PER) {
				// 若自身携带超过自身部队总数 50% 的采矿车
				if (march8cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > ConstProperty.getInstance().getEffect12302SelfNumLimit() * GsConst.EFF_PER) {
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
