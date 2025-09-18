package com.hawk.game.battle.effect.impl.ssstalent;

import java.util.Comparator;
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
 * 【12282】
- 【万分比】【12282】战技持续时间，集结战时，若自身出征防御数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的防御坦克将为己方非防御坦克单位提供光环庇护，处于光环庇护下的单位在受到伤害时，将分摊 XX.XX% 的伤害至提供光环庇护的防御坦克（此庇护效果与卡洛琳的分担效果无法并存，且优先级高于卡洛琳；多个埃琳娜同时存在时，至多有 2 个防御坦克生效）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 该作用号仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 此作用号仅在埃琳娜开启战技后，战技持续期间才生效
  - 若自身出征防御坦克数量超过自身出征部队总数 50%
    - 数量1 = 某玩家出征携带的防御坦克（兵种类型 = 1）数量
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12282SelfNumLimit
        - 配置格式：万分比
  - 若自身出征携带防御坦克数量不低于集结部队总数 5%
    - 数量1 = 某玩家出征携带的防御坦克（兵种类型 = 1）数量
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12282AllNumLimit
        - 配置格式：万分比
  - 自身出征数量最多的防御坦克将为己方非防御坦克单位提供光环庇护，处于光环庇护下的单位在受到伤害时，将分摊 XX.XX% 的伤害至提供光环庇护的防御坦克
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的防御坦克（兵种类型 = 1）生效（若存在多个防御坦克数量一样且最高，取等级高的）（战中不改变目标，若此防御坦克单位在战中阵亡，则失去此效果）
    - 非防御坦克：兵种类型（2~8）
    - 分摊伤害具体是指原单位本身应受到XX点伤害，受到此作用号庇护后转化为
      - 原单位实际受到伤害 = 原伤害 * （1 - 总分摊比率）
      - 发起分摊的防御坦克受到伤害 = 原伤害 * 本防御坦克分摊比率
  - （此庇护效果与卡洛琳的分担效果无法并存，且优先级高于卡洛琳；多个埃琳娜同时存在时，至多有 2 个防御坦克生效）
    - 该庇护效果无法与卡洛琳的分担效果并存
      - 即己方存在作用号【12282】时，则卡洛琳专属作用号【1431】强制无效
    - 单玩家拥有此作用号时，只能提供 1 层效果；集结中存在多个此作用号时，可以同时生效
    - 效果记录在己方部队身上；限制该作用号生效的玩家数量
      - 注：这里限制的是生效玩家数量，若集结中拥有此作用号的玩家数量超出此上限，取作用号数值高的生效
      - 层数上限读取const表，字段effect12282Maxinum
        - 配置格式：绝对值
    - 多个防御坦克同时存在此作用号时，非防御坦克单位受到庇护时，伤害分摊如下
      - 原单位实际受到伤害 = 原伤害 * （1 - 分摊比率1 - 分摊比率2）
      - 发起分摊的防御坦克1受到伤害 = 原伤害 * 本防御坦克分摊比率1
      - 发起分摊的防御坦克2受到伤害 = 原伤害 * 本防御坦克分摊比率2
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12282)
public class Checker12282 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity.getEffVal(EffType.EFF_12091) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		if (parames.type != SoldierType.TANK_SOLDIER_1 || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}
		
		if (parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.TANK_SOLDIER_1)) {
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
				.limit(ConstProperty.getInstance().getEffect12282Maxinum())
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
			if (march8cnt / parames.unitStatic.getTotalCountMarch() >= ConstProperty.getInstance().getEffect12282AllNumLimit() * GsConst.EFF_PER) {
				// 若自身携带超过自身部队总数 50% 的采矿车
				if (march8cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > ConstProperty.getInstance().getEffect12282SelfNumLimit() * GsConst.EFF_PER) {
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
