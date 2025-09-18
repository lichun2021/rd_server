package com.hawk.game.battle.effect.impl.ailinna12081;

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
 * 12085】
- 【万分比】【12085】远程庇护：集结战时，若自身出征防御坦克数量超出自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的防御坦克每第 5 回合，额外为友军远程随机 2 个单位提供防护值为【该防御坦克数量*该防御坦克生命值*XX.XX%】的装甲防护（持续 2 回合；防御坦克数量计算时至多取 100万；装甲防护优先承受伤害，承受伤害达到防护值后消失；同一单位至多可获取 2 层装甲防护；多个埃琳娜存在时，至多有 2 个防御坦克生效）
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后选定可释放防护效果的防御坦克单位，本场战斗全程生效
  - 集结战时，若自身出征防御坦克数量超过自身出征部队总数 50%
    - 数量1 = 某玩家出征携带的防御坦克（兵种类型 = 1）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12085SelfNumLimit
        - 配置格式：万分比
  - 集结战时，若自身出征防御坦克数量不低于集结部队总数 5%
    - 数量1 = 某玩家出征携带的防御坦克（兵种类型 = 1）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12085AllNumLimit
        - 配置格式：万分比
  - 自身出征数量最多的防御坦克每第 5 回合，额外为友军远程随机 2 个单位提供防护值为【该防御坦克数量*该防御坦克生命值*XX.XX%】的装甲防护
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的防御坦克（兵种类型 = 1）生效（若存在多个防御坦克数量一样且最高，取等级高的）（战中不改变目标）
    - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队也生效
    - 友军远程随机 2 个单位
      - 远程部队类型包含有：直升机（兵种类型 = 4）、突击步兵（兵种类型 = 5）、狙击兵（兵种类型 = 6）、攻城车（兵种类型 = 7）
      - 随机个数读取const表，字段effect12085EffectNum
        - 绝对值
      - 只对友军部队生效（先刨除掉该玩家自身的部队）
      - 随机机制如下
        - 筛选出友军且远程且装甲防护层数低的且未满的单位
        - 纯随机出 1 个单位，作为装甲防护的目标
        - 选定后剔除掉该单位，继续上述循环
        - 直至选定单位数达到目标数 或 己方友军远程单位数不足
          - 己方友军远程单位数低于目标数则取所有己方友军远程单位
          - 己方友军无远程单位，则因选择不到目标，无效
    - 该防御坦克数量：为战中当前数量（受所罗门、吉迪恩这种战斗中改变部队数量的机制的影响）；荣耀所罗门的幻影部队则取其母体部队当前的部队数量
      - 此处计算时，防御数量至多取 100万。即护盾值 = min（该防御坦克数量，100万）*该防御坦克生命值*XX.XX%
        - 限制数量读取const表，字段effect12085NumLimit
          - 配置格式：绝对值
    - 该防御坦克生命：为单兵当前实际生命值；实际生命 = 基础生命*（1 + 各类生命加成 - 各类生命减少）；荣耀所罗门的幻影部队则取其母体部队的单兵实际生命值
  - （持续 2 回合；防御坦克数量计算时至多取 100万；装甲防护优先承受伤害，承受伤害达到防护值后消失；同一单位至多可获取 2 层装甲防护；多个埃琳娜存在时，至多有 2 个防御坦克生效）
    - 该护盾为伤害值减少效果，在目标受到伤害时减少所受伤害数值；即实际伤害 = 攻击伤害 - 护盾值
      - 护盾减少伤害后，对应扣除同等数值的护盾值
      - 护盾值扣至0后，护盾消失
    - 持续 2 回合
      - 护盾生效至当前回合结束算1回合
      - 持续 2 回合后，不论当前剩余护盾值，护盾消失
    - 同一单位至多可获取 2 层装甲防护
      - 同一单位受到装甲庇护效果时，限制其可叠加的层数上限
        - 层数达到上限后，无法再次叠加（即先到先得）
      - 叠加时护盾数值累加，持续回合数不变
    - 单玩家拥有此作用号时，只能提供 1 层效果；集结中存在多个此作用号时，可以同时生效
    - 若集结战斗中己方存在多个此作用号效果，限制其生效个数上限（若超出此上限，取作用号数值高的）；即这里只能有 2 个玩家携带的作用号生效
      - 层数上限读取const表，字段effect12085Maxinum
        - 配置格式：绝对值
    - 注：此护盾效果与已有作用号【1633】逻辑类似，该护盾效果与【1633】作用号数值叠加
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12085)
public class Checker12085 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!BattleConst.WarEff.MASS.check(parames.troopEffType) || parames.type != SoldierType.TANK_SOLDIER_1) {
			return CheckerKVResult.DefaultVal;
		}

		if (parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.TANK_SOLDIER_1)) {
			return CheckerKVResult.DefaultVal;
		}

		Map<String, Integer> effPlayerVal = selectPlayer(parames);
		if (!effPlayerVal.containsKey(parames.unity.getPlayerId())) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = effPlayerVal.get(parames.unity.getPlayerId());
		parames.addDebugLog("-【12085】远程庇护：集结战时，若自身出征防御坦克 {} 获得 12085 {}", parames.unity.getSolider().getUUID(), effPer);
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
				.limit(ConstProperty.getInstance().getEffect12085Maxinum())
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
			if (march8cnt / parames.unitStatic.getTotalCountMarch() >= ConstProperty.getInstance().getEffect12085AllNumLimit() * GsConst.EFF_PER) {
				// 若自身携带超过自身部队总数 50% 的采矿车
				if (march8cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > ConstProperty.getInstance().getEffect12085SelfNumLimit() * GsConst.EFF_PER) {
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
