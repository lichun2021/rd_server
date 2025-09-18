package com.hawk.game.battle.effect.impl.ailinna12081;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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
import com.hawk.game.util.GsConst;

/**
 【12081~12083】
- 【万分比】【12081】近战鼓舞：集结战前，若自身出征防御坦克数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身和友军近战部队攻击 +XX.XX%（多个埃琳娜存在时，至多有 2 个防御坦克生效）
- 【万分比】【12082】近战鼓舞：集结战前，若自身出征防御坦克数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身和友军近战部队防御 +XX.XX%（多个埃琳娜存在时，至多有 2 个防御坦克生效）
- 【万分比】【12083】近战鼓舞：集结战前，若自身出征防御坦克数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身和友军近战部队生命 +XX.XX%（多个埃琳娜存在时，至多有 2 个防御坦克生效）
  - 上述3个作用号机制完全一致，加的属性类别不一样，分3个作用号开发；防御坦克兵种类型 = 1
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 该作用号仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 若自身出征防御坦克数量超过自身出征部队总数 50%
    - 数量1 = 某玩家出征携带的防御坦克（兵种类型 = 1）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12081SelfNumLimit、effect12082SelfNumLimit、effect12083SelfNumLimit
        - 配置格式：万分比
  - 若自身出征携带防御坦克数量不低于集结部队总数 5%
    - 数量1 = 某玩家出征携带的防御坦克（兵种类型 = 1）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12081AllNumLimit、effect12082AllNumLimit、effect12083AllNumLimit
        - 配置格式：万分比
  - 自身和友军近战部队攻击/防御/生命 +XX.XX%；（多个埃琳娜存在时，至多有 2 个防御坦克生效）
    - 单玩家拥有此作用号时，只能提供 1 层效果；集结中存在多个此作用号时，可以叠加
    - 自身和友军近战部队
      - 此为【光环效果】，集结战斗中所有己方的近战部队都有此效果加成
      - 近战部队类型包含有：主战坦克（兵种类型 = 2）、防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、轰炸机（兵种类型 = 3）
    - 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
      - 注：这里限制的是生效层数上限，若集结中层数超出此上限，取作用号数值高的
      - 层数上限读取const表，字段effect12081Maxinum、effect12082Maxinum、effect12083Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.ATK)
@EffectChecker(effType = EffType.EFF_12081)
public class Checker12081 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!BattleConst.WarEff.MASS.check(parames.troopEffType) || !isJinzhan(parames.type)) {
			return CheckerKVResult.DefaultVal;
		}

		// 属性最高的二个人
		Integer effPlayerVal;
		Object object = parames.getLeaderExtryParam(getSimpleName());
		if (Objects.nonNull(object)) {
			effPlayerVal = (Integer) object;
		} else {
			effPlayerVal = selectPlayer(parames, ConstProperty.getInstance().getEffect12081SelfNumLimit(), ConstProperty.getInstance().getEffect12081AllNumLimit(),
					ConstProperty.getInstance().getEffect12081Maxinum());
			parames.putLeaderExtryParam(getSimpleName(), effPlayerVal);
		}

		return new CheckerKVResult(effPlayerVal, 0);
	}

	/**数值最高的玩家*/
	public int selectPlayer(CheckerParames parames, int selfNumLimit, int allNumLimit, int maxinum) {
		Map<String, Integer> valMap = new HashMap<>();
		for (BattleUnity unity : parames.unityList) {
			if (valMap.containsKey(unity.getPlayerName())) {
				continue;
			}
			int effvalue = effvalue(unity, parames, selfNumLimit, allNumLimit);
			valMap.put(unity.getPlayerId(), effvalue);
		}

		valMap = valMap.entrySet().stream()
				.sorted(((item1, item2) -> {
					int compare = item2.getValue().compareTo(item1.getValue());
					return compare;
				}))
				.filter(ent -> ent.getValue() > 0)
				.limit(maxinum)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return valMap.values().stream().mapToInt(Integer::intValue).sum();
	}

	public int effvalue(BattleUnity unity, CheckerParames parames, int selfNumLimit, int allNumLimit) {
		try {
			if (unity.getEffVal(effType()) == 0) {
				return 0;
			}
			String playerId = unity.getPlayerId();
			int effPer = 0;
			// 采矿车数量
			int march8cnt = parames.unitStatic.getPlayerSoldierCountMarch().get(playerId, SoldierType.TANK_SOLDIER_1);
			// 若自身出征携带不低于集结部队总数 5% 的采矿车
			if (march8cnt / parames.unitStatic.getTotalCountMarch() >= allNumLimit * GsConst.EFF_PER) {
				// 若自身携带超过自身部队总数 50% 的采矿车
				if (march8cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > selfNumLimit * GsConst.EFF_PER) {
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
