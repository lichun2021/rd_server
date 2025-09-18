package com.hawk.game.battle.effect.impl.heroJiaLaNi12498508;

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
import com.hawk.game.util.GsConst;

/**
 * #12498508 【0608版本】【新英雄】【SSS】【战车双将】杰拉尼制作 https://meego.feishu.cn/ccredalert/story/detail/12498508
 * 
 - 【万分比】【12014】远程支援：集结战斗开始前，若自身出征携带不低于集结部队总数 5% 且 超过自身部队总数 50% 的攻城车，额外为远程友军中出征数量最多的单位增加超能攻击 +XX.XX%*自身属性加成（自身属性加成为自身攻击加成、自身防御加成和自身生命加成的均值）；（多个杰拉尼同时存在时，上述效果可叠加，至多 2 层）
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
      - 注：该作用号效果展示于被施加效果的友军部队上（即在集结中，只有友军为队长且被施加该效果时才能展示）
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 集结战斗开始前，若自身出征携带不低于集结部队总数 5% 的攻城车
    - 数量1 = 某玩家出征携带的攻城车（兵种类型 = 7）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12014AllNumLimit
        - 配置格式：万分比
  - 集结战斗开始前，若自身携带超过自身部队总数 50% 的攻城车
    - 数量1 = 某玩家出征携带的攻城车（兵种类型 = 7）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12014SelfNumLimit
        - 配置格式：万分比
  - 额外为远程友军中出征数量最多的单位增加超能攻击 +XX.XX%*自身属性加成（自身属性加成为自身攻击加成、自身防御加成和自身生命加成的均值）
    - 远程友军
      - 仅对友军部队（集结己方部队，不包含自身）中的远程部队生效
      - 远程部队类型包含有：直升机（兵种类型 = 4）、突击步兵（兵种类型 = 5）、狙击兵（兵种类型 = 6）、攻城车（兵种类型 = 7）
    - 出征数量最多的单位
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
      - 这里按单排战斗单位的数量进行排序后选择
        - 例：玩家A出征泰能突击步兵20万+12级突击步兵10万，玩家B出征泰能突击步兵25万
        - 这里玩家B的泰能突击步兵25万数量最多，该部队获得本作用号效果
      - 若存在多个单位数量一样且最高，取战报列表中排序靠前的
    - 若没有远程友军，则不给任何部队提供加成
    - 为其增加 超能攻击 属性
      - 数值 = 作用值 * 自身属性加成
        - 若配置在英雄上，则 作用值 = 英雄军事值 * 配置系数/10000
      - 自身属性加成 = （自身攻击加成 + 自身防御加成 + 自身生命加成）/3
      - 这里自身攻击/防御/生命加成取的是作用号提供方的攻城车（兵种类型 = 7）兵种的属性
      - 这里为属性加成做下说明
        - 单兵攻击/防御/生命 = （基础数值 + 基础加成）*（1 + 己方各类基础作用号加成 - 敌方各类基础作用号减少）*（1 + 己方各类外围作用号加成 - 敌方各类外围作用号减少）
          - 基础数值：由兵种等级和星级决定，直接配置在兵种表内
          - 基础加成：泰能战士进阶养成特殊效果
          - 己方各类基础作用号加成：各类战场特殊增益效果（如：泰伯战令）
          - 敌方各类基础作用号减少：各类战场特殊减益效果（如：军团模拟战）
          - 己方各类外围作用号加成：常规属性加成（如：科技、装备等）
          - 敌方各类外围作用号减少：各类减益效果（如：英雄亚瑟的减少生命加成的效果）
      - 此处自身攻击/防御/生命加成 = 己方各类外围作用号加成 - 敌方各类外围作用号减少
        - 注：这里取自身属性加成的时机在集结战斗开始时（即各种其他光环、集结增益作用号都算完之后）
  - （多个杰拉尼同时存在时，上述效果可叠加，至多 2 层）
    - 单玩家拥有此作用号时，只能提供 1 层效果；集结中存在多个此作用号时，可以叠加
    - 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
      - 注：这里限制的是生效层数上限，若集结中层数超出此上限，取作用号数值高的；即这里只能有 2 个玩家携带的作用号生效
      - 层数上限读取const表，字段effect12014Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.ATKFIRE)
@EffectChecker(effType = EffType.HERO_12014)
public class Checker12014 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (isJinzhan(parames.type) || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
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
				parames.addDebugLog("-Jelani {} 为 {} 附加 12014 val:{}", ent.getKey(), solider.getUUID(), ent.getValue());
			}
		}

		return new CheckerKVResult(effPer, 0);
	}

	/**最多远程的友军*/
	private BattleUnity theMaxFriend(String playerIdName, CheckerParames parames) {
		BattleUnity maxunity = null;
		for (BattleUnity unity : parames.unityList) {
			if (unity.getPlayerName().equals(playerIdName)) {
				continue;
			}
			if (isJinzhan(unity.getSolider().getType())) {
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
				.limit(ConstProperty.getInstance().getEffect12004Maxinum())
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
			if (march8cnt / parames.unitStatic.getTotalCountMarch() >= ConstProperty.getInstance().getEffect12014AllNumLimit() * GsConst.EFF_PER) {
				// 若自身携带超过自身部队总数 50% 的采矿车
				if (march8cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > ConstProperty.getInstance().getEffect12014SelfNumLimit() * GsConst.EFF_PER) {
					BattleSoldier maxSoldier8 = parames.getPlayerMaxFreeArmy(playerId, SoldierType.CANNON_SOLDIER_7).getSolider();
					int atkper = maxSoldier8.tupleValue(BattleTupleType.Type.ATK, SoldierType.XXXXXXXXXXXMAN).first;
					int defper = maxSoldier8.tupleValue(BattleTupleType.Type.DEF, SoldierType.XXXXXXXXXXXMAN).first;
					int hpper = maxSoldier8.tupleValue(BattleTupleType.Type.HP, SoldierType.XXXXXXXXXXXMAN).first;
					int avg = (atkper + defper + hpper) / 3;
					int eff12022 = unity.getEffVal(EffType.HERO_12022);
					effPer = (int) (avg * GsConst.EFF_PER * unity.getEffVal(effType())) + eff12022;

					parames.addDebugLog("-Jelani {} 12014 仅对友军部队（集结己方部队，不包含自身）中的远程部队生效  {} atk: {} def: {} hp: {} avg: {},eff12022:{} 最终 12014: {}",
							unity.getPlayerName(), maxSoldier8.getUUID(), atkper, defper, hpper, avg, eff12022, effPer);
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
