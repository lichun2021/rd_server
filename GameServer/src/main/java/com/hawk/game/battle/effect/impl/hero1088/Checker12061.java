package com.hawk.game.battle.effect.impl.hero1088;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
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
12061】
- 【万分比】【12061】集结战斗开始前，若自身出征直升机（兵种类型 = 4）数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的直升机单位在本场战斗中获得如下效果: 每释放 1 次【黑鹰轰炸】后，使自身和友军空军部队轰炸概率 +XX.XX%（该效果可叠加，至多 XX%）；（多个新英雄同时存在时，至多有 2 个直升机单位生效）
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 集结战斗开始前，若自身出征直升机（兵种类型 = 4）数量超过不低于集结部队总数 5%
    - 数量1 = 某玩家出征携带的直升机（兵种类型 = 4）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12061AllNumLimit
        - 配置格式：万分比
  - 集结战斗开始前，若自身出征直升机（兵种类型 = 4）数量超过自身出征部队总数 50%
    - 数量1 = 某玩家出征携带的直升机（兵种类型 = 4）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12061SelfNumLimit
        - 配置格式：万分比
  - 自身出征数量最多的直升机单位在本场战斗中获得如下效果
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的直升机（兵种类型 = 4）生效（若存在多个直升机数量一样且最高，取等级高的）
    - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队也生效
  - 每释放 1 次【黑鹰轰炸】后，使自身和友军空军部队轰炸概率 +XX.XX%（该效果可叠加，至多 XX%）
    - 【黑鹰轰炸】【id = 403】为直升机兵种技能
    - 空军部队类型包含有：直升机（兵种类型 = 4）、轰炸机（兵种类型 = 3）
    - 轰炸概率
      - 对直升机兵种，是指其兵种技能【黑鹰轰炸】【id = 403】的发动概率
        - 即【黑鹰轰炸】实际概率 = 基础概率 + 其他加成 + 【本作用值】
          - 基础概率读取const表，字段trigger
      - 对轰炸机兵种，是指其兵种技能【俯冲轰炸】【id = 303】的发动概率
        - 即【俯冲轰炸】实际概率 = 基础概率 + 其他加成 + 【本作用值】
          - 基础概率读取const表，字段trigger
    - 该效果被附加后，持续至本场战斗结束
    - 概率叠加的效果记录在自身和友军部队身上，独立记录
      - 集结中存在多个此作用号时，可以数值叠加；限制该作用号数值上限
        - 数值上限const表，字段effect12061MaxValue
          - 配置格式：万分比
  - （多个新英雄同时存在时，至多有 2 个直升机单位生效）
    - 若集结战斗中己方存在多个此作用号效果，限制其生效个数上限（若超出此上限，取作用号数值高的，若作用号数值相同，则取战报列表中排序靠前的）；即这里只能有 2 个玩家携带的作用号生效
      - 层数上限读取const表，字段effect12061Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12061)
public class Checker12061 implements IChecker {

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

		return new CheckerKVResult(parames.unity.getEffVal(effType()), 0);
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
				.limit(ConstProperty.getInstance().getEffect12061Maxinum())
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
			if (march8cnt / parames.unitStatic.getTotalCountMarch() >= ConstProperty.getInstance().getEffect12061AllNumLimit() * GsConst.EFF_PER) {
				// 若自身携带超过自身部队总数 50% 的采矿车
				if (march8cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > ConstProperty.getInstance().getEffect12061SelfNumLimit() * GsConst.EFF_PER) {
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
