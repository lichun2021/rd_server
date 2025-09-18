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
 * 【12333~12334】
- 【万分比】【12333~12334】集结战斗开始前，若自身出征攻城车数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的攻城车于本场战斗中获取如下效果（多个威尔森同时存在时，至多有 2 个攻城车单位生效）;在战斗处于奇数回合时，开启火力全开模式:  于本回合为至多 2 个友军远程单位提供火力支援，使其超能攻击、攻击增加 【XX.XX% + 火力值*XX.XX%】（该效果无法叠加）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 其中【12333】为主作用号，主导上述所有作用号逻辑；【12334】为辅作用号，仅关联计算属性加成效果
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 集结战斗开始前，若自身出征携带不低于集结部队总数 5% 的攻城车
    - 数量1 = 某玩家出征携带的攻城车（兵种类型 = 7）数量
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12333AllNumLimit
        - 配置格式：万分比
  - 集结战斗开始前，若自身携带超过自身出征部队总数 50% 的攻城车
    - 数量1 = 某玩家出征携带的攻城车（兵种类型 = 7）数量
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12333SelfNumLimit
        - 配置格式：万分比
  - 自身出征数量最多的攻城车
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的攻城车（兵种类型 = 7）生效（若存在多个攻城车数量一样且最高，取等级高的）
      - 在战斗开始前进行判定，选中目标后战中不再变化
    - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队无效！
  - 在战斗处于奇数回合时，开启火力全开模式: 
    - 即在战斗处于第1、3、....奇数回合时，该作用号才生效
  - 于本回合为至多 2 个友军远程单位提供火力支援，使其超能攻击、攻击增加 【XX.XX% + 火力值*XX.XX%】（该效果无法叠加）
    - 在本回合开始时进行判定，确定目标后，本回合内不再变化；具体随机规则如下
      - 先选1个未处于本增益状态下的远程友军单位，刨除掉已选中的之后再继续选择，直至己方友军远程单位数不足或已达到可提供的单位数上限
    - 此加成为外围属性加成效果，与其他作用号累加计算
      - 即 实际属性 = 基础属性*（1 + 其他作用号加成 +【本作用值】）
    - 远程部队类型包含有：直升机（兵种类型 = 4）、突击步兵（兵种类型 = 5）、狙击兵（兵种类型 = 6）、攻城车（兵种类型 = 7）
    - 只能选择友军单位（即无法给自身提供该作用号效果）
    - 该效果无法叠加
      - 即某单位挂上该作用号后，后续再生效此作用号时，数值不变且持续回合数不变
    - 至多选中的友军单位数量读取const表，字段effect12333AffectNum
      - 配置格式：绝对值
  - （多个威尔森同时存在时，至多有 2 个攻城车单位生效）
    - 单玩家拥有此作用号时，只能生效 1 个攻城车单位；集结中存在多个此作用号时，可以同时生效
    - 限制该作用号生效人数上限
      - 注：这里限制的是生效人数上限，若集结中超出此上限，取作用号数值高的，若作用号数值相同，取战报列表中排序靠前的；即这里只能有 2 个玩家携带的作用号生效
      - 层数上限读取const表，字段effect12333Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12333)
public class Checker12333 implements IChecker {
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
				.limit(ConstProperty.getInstance().getEffect12333Maxinum())
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
			if (march8cnt / parames.unitStatic.getTotalCountMarch() >= ConstProperty.getInstance().getEffect12333AllNumLimit() * GsConst.EFF_PER) {
				// 若自身携带超过自身部队总数 50% 的采矿车
				if (march8cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > ConstProperty.getInstance().getEffect12333SelfNumLimit() * GsConst.EFF_PER) {
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
