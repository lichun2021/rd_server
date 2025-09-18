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
- 【万分比】【12034】集结战斗时，自身和友军所有空军部队伤害 +XX.XX%（多个玩家拥有此作用号时，至多有 2 个玩家生效）
- 战报相关
  - 于战报中展示
  - 合并至精简战报中
    - 注：该作用号效果展示于被施加效果的友军部队上（即在集结中，只有友军为队长且被施加该效果时才能展示）
- 该作用号仅对集结战斗生效（包含集结进攻和集结防守）
- 在战斗开始前判定，满足条件后本次战斗全程生效
- 空军部队包含直升机（兵种类型 = 4）和轰炸机（兵种类型 = 3）
- 若没有空军部队，则不给任何部队提供加成
- 此伤害加成与其他伤害加成累加计算；即 实际伤害 = 基础伤害*（1 + 各类伤害加成 +【本作用值】）
- （多个玩家拥有此作用号时，至多有 2 个玩家生效）
  - 单玩家拥有此作用号时，只能提供 1 层效果；集结中存在多个此作用号时，可以叠加
  - 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
    - 注：这里限制的是生效层数上限，若集结中层数超出此上限，取作用号数值高的；即这里只能有 2 个玩家携带的作用号生效
    - 层数上限读取const表，字段effect12034Maxinum
      - 配置格式：绝对值
 */

@BattleTupleType(tuple = Type.HURT)
@EffectChecker(effType = EffType.EFF_12034)
public class Checker12034 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!parames.solider.isPlan() || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
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
			effPer += ent.getValue();
			BattleSoldier solider = parames.unity.getSolider();
			parames.addDebugLog("- {} 为 {} 附加 12034 val:{}", ent.getKey(), solider.getUUID(), ent.getValue());
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
			valMap.put(unity.getPlayerName(), effvalue);
		}

		valMap = valMap.entrySet().stream()
				.sorted(((item1, item2) -> {
					int compare = item2.getValue().compareTo(item1.getValue());
					return compare;
				}))
				.filter(ent -> ent.getValue() > 0)
				.limit(ConstProperty.getInstance().getEffect12034Maxinum())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return valMap;
	}

	private int effvalue(BattleUnity unity, CheckerParames parames) {
		return unity.getEffVal(effType());
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}