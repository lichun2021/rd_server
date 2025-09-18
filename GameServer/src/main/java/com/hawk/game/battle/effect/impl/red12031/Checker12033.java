package com.hawk.game.battle.effect.impl.red12031;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hawk.os.HawkException;

import com.hawk.game.battle.BattleSoldier;
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

/**
- 【万分比】【12033】集结战斗开始前，自身出征数量最多的防御坦克（兵种类型 = 1）为近战友军中出征数量最多的单位增加防御、生命 +XX.XX%（多个玩家拥有此作用号时，至多有 2 个玩家生效）
- 战报相关
  - 于战报中展示
  - 合并至精简战报中
    - 注：该作用号效果展示于被施加效果的友军部队上（即在集结中，只有友军为队长且被施加该效果时才能展示）
    - 注：这里展示逻辑上与其他常规作用号不同，可参考已有作用号【12004】进行处理
- 该作用号仅对集结战斗生效（包含集结进攻和集结防守）
- 在战斗开始前判定，满足条件后本次战斗全程生效
- 近战友军
  - 仅对友军部队（集结己方部队，不包含自身）中的近战部队生效
  - 近战部队类型包含有：主战坦克（兵种类型 = 2）、防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、轰炸机（兵种类型 = 3）
- 出征数量最多的单位
  - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
  - 这里按单排战斗单位的数量进行排序后选择
    - 例：玩家A出征泰能主战坦克20万+11级主战坦克10万，玩家B出征泰能主战坦克25万
    - 这里玩家B的泰能主战坦克25万数量最多，该部队获得本作用号效果
  - 若存在多个单位数量一样且最高，取战报列表中排序靠前的
- 若没有近战友军，则不给任何部队提供加成
- 为其增加 防御和生命 属性，同与常规外围属性加成累加计算；即实际防御/生命 = 基础防御/生命*（1 + 各类加成 + 【本作用值】）
- （多个玩家拥有此作用号时，至多有 2 个玩家生效）
  - 单玩家拥有此作用号时，只能提供 1 层效果；集结中存在多个此作用号时，可以叠加
  - 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
    - 注：这里限制的是生效层数上限，若集结中层数超出此上限，取作用号数值高的；即这里只能有 2 个玩家携带的作用号生效
    - 层数上限读取const表，字段effect12033Maxinum
      - 配置格式：绝对值
 */

@BattleTupleType(tuple = { Type.HP, Type.DEF })
@EffectChecker(effType = EffType.EFF_12033)
public class Checker12033 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (isYuanCheng(parames.type) || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
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

		int effPer = 0;
		for (Entry<String, Integer> ent : effPlayerVal.entrySet()) {
			if (theMaxFriend(ent.getKey(), parames) == parames.unity) {
				effPer += ent.getValue();
				BattleSoldier solider = parames.unity.getSolider();
				parames.addDebugLog("- {} 为 {} 附加 12033 val:{}", ent.getKey(), solider.getUUID(), ent.getValue());
			}
		}

		return new CheckerKVResult(effPer, 0);
	}

	/**最多进展的友军*/
	private BattleUnity theMaxFriend(String playerIdName, CheckerParames parames) {
		BattleUnity maxunity = null;
		for (BattleUnity unity : parames.unityList) {
			if (unity.getPlayerName().equals(playerIdName)) {
				continue;
			}
			if (!isJinzhan(unity.getSolider().getType())) {
				continue;
			}

			if (maxunity == null || unity.getMarchCnt() > maxunity.getMarchCnt()) {
				maxunity = unity;
			}
		}

		return maxunity;
	}

	/**数值最高的玩家*/
	private Map<String, Integer> selectPlayer(CheckerParames parames) {
		Map<String, Integer> valMap = new HashMap<>();
		for (BattleUnity unity : parames.unityList) {
			if (valMap.containsKey(unity.getPlayerName())) {
				continue;
			}
			int effvalue = effvalue(unity, parames);
			valMap.put(unity.getPlayerName(), effvalue);
		}

		valMap = valMap.entrySet().stream()
				.sorted(((item1, item2) -> {
					int compare = item2.getValue().compareTo(item1.getValue());
					return compare;
				}))
				.filter(ent -> ent.getValue() > 0)
				.limit(ConstProperty.getInstance().getEffect12033Maxinum())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return valMap;
	}

	private int effvalue(BattleUnity unity, CheckerParames parames) {
		try {
			if (unity.getEffVal(effType()) == 0) {
				return 0;
			}
			String playerId = unity.getPlayerId();
			// 采矿车数量
			int march8cnt = parames.unitStatic.getPlayerSoldierCountMarch().get(playerId, SoldierType.TANK_SOLDIER_1);
			if(march8cnt == 0){
				return 0;
			}
			return unity.getEffVal(effType());
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