package com.hawk.game.battle.effect.impl.hero1094;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.hawk.game.battle.BattleUnity;
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
 * - 【万分比】【12151】任命在战备参谋部时，集结战斗开始前，若集结全队出征防御坦克数量不低于集结部队总数 10%，己方全体近战部队攻击、防御、生命增加【固定数值+XX.XX%】（多个安妮同时存在时，上述效果可叠加，至多 7 层）
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 若集结全队出征防御坦克数量不低于集结部队总数 10%
    - 数量1 = 集结全队出征携带的防御坦克（兵种类型 = 1）数量
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12151AllNumLimit
        - 配置格式：万分比
  - 己方全体近战部队攻击、防御、生命增加 【固定数值+XX.XX%】
    - 该作用号数值效果为【光环】效果，生效到己方全体近战部队上
      - 近战部队类型包含有：主战坦克（兵种类型 = 2）、防御坦克（兵种类型 = 1）、轰炸机（兵种类型 = 3）、采矿车（兵种类型 = 8）
    - 单个玩家提供数值 = 该作用号固定数值 + 英雄参谋值*技能系数/10000
      - 该作用号固定数值读取const表，字段effect12151BaseVaule
        - 配置格式：万分比
    - 该作用号为常规外围属性加成效果，与其他作用号累加计算
      - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】）
  - （多个安妮同时存在时，上述效果可叠加，至多 7 层）
    - 单玩家拥有此作用号时，只能提供 1 层效果；集结中存在多个此作用号时，可以叠加
    - 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
      - 注：这里限制的是生效层数上限，若集结中层数超出此上限，取作用号数值高的；即这里只能有 7 个玩家携带的作用号生效
      - 层数上限读取const表，字段effect12151Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = { Type.ATK, Type.DEF, Type.HP })
@EffectChecker(effType = EffType.HERO_12151)
public class Checker12151 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!isJinzhan(parames.type)) {
			return CheckerKVResult.DefaultVal;
		}

		// 属性最高的二个人
		Integer effPer;
		Object object = parames.getLeaderExtryParam(getSimpleName());
		if (Objects.nonNull(object)) {
			effPer = (Integer) object;
		} else {
			effPer = selectPlayer(parames);
			parames.putLeaderExtryParam(getSimpleName(), effPer);
		}

		return new CheckerKVResult(effPer, 0);

	}

	/**数值最高的玩家*/
	private int selectPlayer(CheckerParames parames) {
		double total = parames.unitStatic.getTotalCountMarch();
		double tankCnt = parames.unitStatic.getArmyCountMapMarch().get(SoldierType.TANK_SOLDIER_1);
		if (tankCnt / total < ConstProperty.getInstance().getEffect12151AllNumLimit() * GsConst.EFF_PER) {
			return 0;
		}

		Map<String, Integer> valMap = new HashMap<>();
		for (BattleUnity unity : parames.unityList) {
			if (valMap.containsKey(unity.getPlayerName())) {
				continue;
			}
			int effvalue = unity.getEffVal(effType());
			valMap.put(unity.getPlayerName(), effvalue);
		}

		int val = valMap.entrySet().stream()
				.sorted(((item1, item2) -> {
					int compare = item2.getValue().compareTo(item1.getValue());
					return compare;
				}))
				.filter(ent -> ent.getValue() > 0)
				.limit(ConstProperty.getInstance().getEffect12151Maxinum())
				.mapToInt(item -> item.getValue().intValue()).sum();
		return val;
	}

}
