package com.hawk.game.battle.effect.impl.hero1106;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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
 * - 【万分比】【12464】集结战斗开始前，若自身出征直升机数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的直升机于本场战斗中获取如下效果（多个薇拉同时存在时，至多有 2（effect12464Maxinum） 个直升机单位生效）:坐镇中场: 使己方全体空军和步兵单位攻击、防御、生命增加 【固定值(effect12464BaseVaule) + XX.XX%【12464】*自身属性加成】（自身属性加成为自身攻击加成、防御加成和生命加成的均值）
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 己方全体空军和步兵单位攻击、防御、生命增加 【固定数值+XX.XX%*自身属性加成】
    - 该作用号数值效果为【光环】效果，生效到己方全体空军和步兵单位上
      - 空军单位包含有：直升机（兵种类型 = 4） 和 轰炸机（兵种类型 = 3）
      - 步兵单位包含有：狙击兵（兵种类型 = 6）和 突击步兵（兵种类型 = 5）
    - 单个玩家提供数值 = 该作用号固定数值 + 自身属性加成 * 技能系数/10000
      - 自身属性加成为自身攻击加成、防御加成和生命加成的均值
        - 注：这部分为已有逻辑，可见杰拉尼专属作用号【12004】
[图片]
      - 该作用号固定数值读取const表，字段effect12464BaseVaule
        - 配置格式：万分比
    - 该作用号为常规外围属性加成效果，与其他作用号累加计算
      - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】）
  - （多个薇拉同时存在时，至多有 2 个直升机单位获得上述效果）
    - 单玩家拥有此作用号时，只能提供 1 层效果；集结中存在多个此作用号时，可以叠加
    - 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
      - 注：这里限制的是生效层数上限，若集结中层数超出此上限，取作用号数值高的；即这里只能有 2 个玩家携带的作用号生效
      - 层数上限读取const表，字段effect12464Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = { Type.SOLDIER_SKILL })
@EffectChecker(effType = EffType.HERO_12464)
public class Checker12464 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}

		boolean bfalse = parames.type == SoldierType.PLANE_SOLDIER_3 || parames.type == SoldierType.PLANE_SOLDIER_4 || parames.type == SoldierType.FOOT_SOLDIER_5
				|| parames.type == SoldierType.FOOT_SOLDIER_6;
		if (!bfalse) {
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
			int effvalue = effvalue(unity, parames);
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
			if (march8cnt / parames.unitStatic.getTotalCountMarch() >= ConstProperty.getInstance().getEffect12461AllNumLimit() * GsConst.EFF_PER) {
				// 若自身携带超过自身部队总数 50% 的采矿车
				if (march8cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > ConstProperty.getInstance().getEffect12461SelfNumLimit() * GsConst.EFF_PER) {
					BattleSoldier maxSoldier8 = parames.getPlayerMaxFreeArmy(playerId, SoldierType.PLANE_SOLDIER_4).getSolider();
					int atkper = maxSoldier8.tupleValue(BattleTupleType.Type.ATK, SoldierType.XXXXXXXXXXXMAN).first;
					int defper = maxSoldier8.tupleValue(BattleTupleType.Type.DEF, SoldierType.XXXXXXXXXXXMAN).first;
					int hpper = maxSoldier8.tupleValue(BattleTupleType.Type.HP, SoldierType.XXXXXXXXXXXMAN).first;
					int avg = (atkper + defper + hpper) / 3;
					effPer = (int) (avg * GsConst.EFF_PER * unity.getEffVal(effType())) + ConstProperty.getInstance().getEffect12464BaseVaule();
					parames.addDebugLog(
							"-坐镇中场: 使己方全体空军和步兵单位攻击、防御、生命增加 【固定值(effect12464BaseVaule) + XX.XX%【12464】*自身属性加成】 {} atk: {} def: {} hp: {} avg: {}, 12464BaseVaule:{} 最终 12464: {}",
							maxSoldier8.getUUID(), atkper, defper, hpper, avg, ConstProperty.getInstance().getEffect12464BaseVaule(), effPer);
				}
			}
			return effPer;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

}
