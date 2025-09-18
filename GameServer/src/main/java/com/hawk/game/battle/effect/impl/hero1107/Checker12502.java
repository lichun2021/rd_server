package com.hawk.game.battle.effect.impl.hero1107;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;

/**
 * - 【万分比】【12502】任命在战备参谋部时，集结战斗开始前，敌方集结全队出征兵种中，数量最多的兵种，数量每达到 X (effect12502AllNumLimit)万，自身步兵受到伤害时，伤害减少 +XX.XX%(12502)（该效果可叠加，至多 XX(effect12502AllNumLimit) 层）
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 敌方集结全队出征兵种中，数量最多的兵种，数量每达到 X 万
    - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
      - 敌方为非集结时，不生效
    - 判断敌方集结全队出阵兵种中，单位数量最多的兵种
      - 兵种共有8种：防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、主战坦克（兵种类型 = 2）、轰炸机（兵种类型 = 3）、直升机（兵种类型 = 4）、突击步兵（兵种类型 = 5）、狙击兵（兵种类型 = 6）、攻城车（兵种类型 = 7）
      - 同种兵种不同等级时，算作同一兵种
      - 实际生效层数 = 该兵种数量 / X
        - 计算结果向下取整
        - 指定数值读取const表，字段effect12502AllNumLimit
          - 配置格式：绝对值
  - 自身步兵受到攻击时，伤害减少 +XX.XX%
    - 自身：这里仅对自己出征的步兵生效
    - 兵种包含有：突击步兵（兵种类型 = 5）、狙击兵（兵种类型 = 6）
    - 该伤害减少效果与其他伤害减少效果累乘计算；即实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 己方某伤害减少）*（1 - 本作用值伤害减少）
  - （该效果可叠加，至多 XX  层）
    - 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
      - 层数上限读取const表，字段effect12502Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = { Type.REDUCE_HURT_PCT })
@EffectChecker(effType = EffType.HERO_12502)
public class Checker12502 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		
		if (!isFoot(parames.type) || !BattleConst.WarEff.MASS.check(parames.troopEffType) || parames.tarStatic.getPlayerArmyCountMap().size() == 1) {
			return CheckerKVResult.DefaultVal;
		}

		// 属性最高的二个人
		int effPer = selectPlayer(parames);

		return new CheckerKVResult(effPer, 0);

	}

	/**数值最高的玩家*/
	private int selectPlayer(CheckerParames parames) {
		int tankCnt = parames.tarStatic.getArmyCountMapMarch().asMap().values().stream().mapToInt(Long::intValue).max().getAsInt();

		int cen = Math.min(tankCnt / ConstProperty.getInstance().getEffect12502AllNumLimit(), ConstProperty.getInstance().getEffect12502Maxinum());
		return cen * parames.unity.getEffVal(effType());
	}

}
