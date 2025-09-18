package com.hawk.game.battle.effect.impl.hero1100;

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
 * 【12331~12332】
- 【万分比】【12331~12332】集结战斗开始前，若自身出征攻城车数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的攻城车于本场战斗中获取如下效果（多个威尔森同时存在时，至多有 2 个攻城车单位生效）;在战斗处于奇数回合时，开启火力全开模式: 使自身攻击所造成的伤害增加 【XX.XX% + 火力值*XX.XX%】，且在每攻击命中 1 次敌方单位后，增加自身 1 点援护值（该数值可累计，至多 100点）; 
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 其中【12331】为主作用号，主导上述所有作用号逻辑；【12332】为辅作用号，仅关联计算伤害加成效果
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 集结战斗开始前，若自身出征携带不低于集结部队总数 5% 的攻城车
    - 数量1 = 某玩家出征携带的攻城车（兵种类型 = 7）数量
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12331AllNumLimit
        - 配置格式：万分比
  - 集结战斗开始前，若自身携带超过自身出征部队总数 50% 的攻城车
    - 数量1 = 某玩家出征携带的攻城车（兵种类型 = 7）数量
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12331SelfNumLimit
        - 配置格式：万分比
  - 自身出征数量最多的攻城车
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的攻城车（兵种类型 = 7）生效（若存在多个攻城车数量一样且最高，取等级高的）
      - 在战斗开始前进行判定，选中目标后战中不再变化
    - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队也生效
  - 在战斗处于奇数回合时，开启火力全开模式: 
    - 即在战斗处于第1、3、....奇数回合时，该作用号才生效
  - 使自身攻击所造成的伤害增加 【XX.XX% + 火力值*XX.XX%】
    - 此伤害加成为额外伤害加成效果，与其他作用号累加计算
      - 即 实际伤害 = 基础伤害*（1 + 其他作用号加成 +【本作用值】）
  - 且在每攻击命中 1 次敌方单位后，增加自身 1 点援护值（该数值可累计，至多 100点）
    - 攻击每命中1次敌方单位，则增加 1 点援护值
      - 若为范围攻击，则计算多次
    - 增加援护值读取const表，字段effect12331AddEscortPoint
      - 配置格式：绝对值
    - 另外援护值有数量上限，读取const表，字段effect12331MaxEscortPoint
      - 配置格式：绝对值
  - （多个威尔森同时存在时，至多有 2 个攻城车单位生效）
    - 单玩家拥有此作用号时，只能生效 1 个攻城车单位；集结中存在多个此作用号时，可以同时生效
    - 限制该作用号生效人数上限
      - 注：这里限制的是生效人数上限，若集结中超出此上限，取作用号数值高的，若作用号数值相同，取战报列表中排序靠前的；即这里只能有 2 个玩家携带的作用号生效
      - 层数上限读取const表，字段effect12331Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12331)
public class Checker12331 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {

		if (parames.type != SoldierType.CANNON_SOLDIER_7 || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}
		
		if (parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.CANNON_SOLDIER_7)) {
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
				.limit(ConstProperty.getInstance().getEffect12331Maxinum())
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
			int march8cnt = parames.unitStatic.getPlayerSoldierCountMarch().get(playerId, SoldierType.CANNON_SOLDIER_7);
			// 若自身出征携带不低于集结部队总数 5% 的采矿车
			if (march8cnt / parames.unitStatic.getTotalCountMarch() >= ConstProperty.getInstance().getEffect12331AllNumLimit() * GsConst.EFF_PER) {
				// 若自身携带超过自身部队总数 50% 的采矿车
				if (march8cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > ConstProperty.getInstance().getEffect12331SelfNumLimit() * GsConst.EFF_PER) {
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
