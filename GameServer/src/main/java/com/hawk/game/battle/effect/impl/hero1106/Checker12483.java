package com.hawk.game.battle.effect.impl.hero1106;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 *  【12483】
- 【万分比】【12483】触发坐镇中场时，使己方全体空军和步兵单位防御、生命额外 +XX.XX%【12483】
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 此作用号机制为作用号【12464】生效的目标单位额外增加属性加成，作用号【12464】确定目标后挂上
  - 此加成为外围属性加成效果，与其他作用号累加计算
    - 即 实际属性 = 基础属性*（1 + 其他作用号加成 +【本作用值】）
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12483)
public class Checker12483 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12464) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		// 属性最高的二个人
		Integer effPer;
		String key = getSimpleName();
		Object object = parames.getLeaderExtryParam(key);
		if (Objects.nonNull(object)) {
			effPer = (Integer) object;
		} else {
			effPer = selectPlayer(parames);
			parames.putLeaderExtryParam(key, effPer);
		}

		return new CheckerKVResult(effPer, 0);
	}

	/**数值最高的玩家*/
	private int selectPlayer(CheckerParames parames) {
		Map<String, Integer> valMap = new LinkedHashMap<>();
		for (BattleUnity unity : parames.unityList) {
			if (valMap.containsKey(unity.getPlayer())) {
				continue;
			}
			int effvalue = unity.getEffVal(effType());
			valMap.put(unity.getPlayerId(), effvalue);
		}

		int val = valMap.entrySet().stream()
				.sorted(((item1, item2) -> {
					int compare = item2.getValue().compareTo(item1.getValue());
					return compare;
				}))
				.filter(ent -> ent.getValue() > 0)
				.limit(ConstProperty.getInstance().getEffect12464Maxinum())
				.mapToInt(item -> item.getValue().intValue()).sum();
		return val;
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
