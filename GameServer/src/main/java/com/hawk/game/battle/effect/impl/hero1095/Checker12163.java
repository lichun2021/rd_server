package com.hawk.game.battle.effect.impl.hero1095;

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
 * 12163】
- 【万分比】【12163】集结战斗时，若自身出征采矿车数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的采矿车于本场战斗中获取如下效果（多个杰西卡同时存在时，至多有 2 个采矿车单位生效）：引爆：在鼓风完成后，若杰西卡和杰拉尼同时出征，则采矿车额外向敌方处于【燃烧状态】的随机 5 个近战单位投掷油爆弹，对其造成爆炸伤害（伤害率XX.XX%），并使其进入【损坏状态】，持续 3 回合（引爆后，被引爆的敌方单位，其【燃烧状态】解除）
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
      - 指定数值读取const表，字段effect12163AllNumLimit
        - 配置格式：万分比
  - 集结战斗开始前，若自身携带超过自身部队总数 50% 的采矿车
    - 数量1 = 某玩家出征携带的采矿车（兵种类型 = 8）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12163SelfNumLimit
        - 配置格式：万分比
  - 自身出征数量最多的采矿车在本场战斗中获得如下效果
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的采矿车（兵种类型 = 8）生效（若存在多个采矿车数量一样且最高，取等级高的）
    - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队不生效
  - 引爆：在鼓风完成后，若杰西卡和杰拉尼同时出征，则采矿车额外向敌方处于【燃烧状态】的随机 5 个近战单位投掷油爆弹，对其造成爆炸伤害（伤害率XX.XX%），并使其进入【损坏状态】，持续 3 回合（引爆后，被引爆的敌方单位，其【燃烧状态】解除）
    - 此作用号机制绑定作用号【12162】，在【12162】所有回合的效果执行完成后的当前回合结束时，拥有此作用号且携带英雄杰拉尼（id = 1086）的采矿车生效
    - 敌方处于【燃烧状态】的随机 5 个近战单位进行1轮攻击
      - 近战部队包含：主战坦克（兵种类型 = 2）、防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、轰炸机（兵种类型 = 3）
      - 5 个单位读取const表，字段effect12163AtkNum
        - 配置格式：绝对值
      - 伤害率（XX.XX%）
        - 即 实际伤害 = 本次伤害率 * 基础伤害
      - 这里随机为纯随机，就是1轮不重复的选5个单位，不足5个则全取
      - 攻击完成后，被攻击的5个目标的【燃烧状态】解除
    - （多个杰西卡同时存在时，至多有 2 个采矿车单位获得上述效果）
      - 注：这里限制的是集结战斗中某方拥有此作用号效果的采矿车的数量；若集结中超出此上限，取数值高的；即这里只能有 2 个玩家携带的作用号生效
        - 另外当有2个玩家生效时，则另外1个玩家按次序生效，也进行1轮攻击
        - 层数上限读取const表，字段effect12163Maxinum
          - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12163)
public class Checker12163 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!BattleConst.WarEff.MASS.check(parames.troopEffType) || parames.type != SoldierType.CANNON_SOLDIER_8) {
			return CheckerKVResult.DefaultVal;
		}

		if (parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.CANNON_SOLDIER_8)) {
			return CheckerKVResult.DefaultVal;
		}

		Map<String, Integer> effPlayerVal = selectPlayer(parames);
		if (!effPlayerVal.containsKey(parames.unity.getPlayerId())) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = effPlayerVal.get(parames.unity.getPlayerId());
		parames.addDebugLog("-：引爆：在汲火完成后 {} 获得 12162 {}", parames.unity.getSolider().getUUID(), effPer);
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
				.limit(ConstProperty.getInstance().getEffect12163Maxinum())
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
			if (march8cnt / parames.unitStatic.getTotalCountMarch() >= ConstProperty.getInstance().getEffect12163AllNumLimit() * GsConst.EFF_PER) {
				// 若自身携带超过自身部队总数 50% 的采矿车
				if (march8cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > ConstProperty.getInstance().getEffect12163SelfNumLimit() * GsConst.EFF_PER) {
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
