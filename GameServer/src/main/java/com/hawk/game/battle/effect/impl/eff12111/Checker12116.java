package com.hawk.game.battle.effect.impl.eff12111;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;

/**
 *  - 【万分比】【12116】每第 5 回合开始时，为己方累计战损比率排序前 1 名的战斗单位提供战场保护，使其受到伤害减少 +XX.XX%（该效果作用单位数和效果数值均可叠加，至多 X 层；持续 3 回合）
  - 战报相关
    - 战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 累计战损比率
    - 此战损为某战斗单位于战斗中的“死亡”数量，与战斗结束时的实际战损结算完全是两码事
    - 战损比率 = 【100% - 当前数量/战斗初始数量 *100%】
      - 注：真实参与战斗的数量，在战斗开始时判定，即 参谋军威技能、所罗门这种战前改变参战单位数量的机制，对战斗初始数量有影响
      - 注：此为比率数值，最终取值合法区间为【0,100%】
    - 此效果绑定在各不同战斗单位（即兵种类型 = 1~8）上，各自独立计算
  - 每第 5 回合开始时
    - 间隔回合参数读取读取const表，字段effect12116IntervalRound
      - 配置格式：绝对值
  - 使其受到伤害减少 +XX.XX%
    - 该作用号数值效果为【伪光环】效果，生效到己方选中的指定战斗单位上
    - 该作用号为伤害减少效果，与其他作用号累乘计算
      - 即 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【本作用值】）
  - 持续 3 回合
    - 本回合被附加到下回合开始算 1 回合
    - 持续回合数读取const表，字段effect12116ContinueRound
      - 配置格式：绝对值
  - （该效果作用单位数和效果数值均可叠加，至多 X 层）
    - 单玩家拥有此作用号时，只能提供 1 层效果；集结中存在多个此作用号时，可以叠加
      - 作用己方战斗单位数量可以叠加
      - 作用号数值也可以叠加
        - 即集结时，10个玩家带上此作用号，可以给10个战斗单位施加此效果，且其数值为这10个玩家的数值和值
      - 注：这里限制的是生效层数上限，若集结中层数超出此上限，取作用号数值高的；即这里只能有 X 个玩家携带的作用号生效
    - 层数上限读取const表，字段effect12116Maxinum
      - 配置格式：绝对值
    - 另外对于此作用号实际施加的战斗单位，该战斗单位仅可生效 1 层该作用号
      - 优先取累计战损比率大的
        - 若累计战损比率最大且相等，优先取兵种等级高的
          - 若战损比率最大且相等，且兵种等级也最高且相等，优先取战报列表中排序靠前的
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12116)
public class Checker12116 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		// 属性最高的二个人
		int effPer;
		String key = getSimpleName();
		Object object = parames.getLeaderExtryParam(key);
		if (Objects.nonNull(object)) {
			HawkTuple2<Integer,Integer> tuple = (HawkTuple2<Integer, Integer>) object;
			effPer = tuple.first;
			parames.solider.setEffect12116Maxinum(tuple.second);
		} else {
			Map<String, Integer> valMap = selectPlayer(parames);
			effPer = valMap.values().stream().mapToInt(Integer::intValue).sum();
			HawkTuple2<Integer, Integer> tuple = HawkTuples.tuple(effPer, valMap.size());
			parames.solider.setEffect12116Maxinum(tuple.second);
			
			parames.putLeaderExtryParam(key, tuple);
		}

		return new CheckerKVResult(effPer, 0);
	}

	/**数值最高的玩家*/
	private Map<String, Integer> selectPlayer(CheckerParames parames) {
		Map<String, Integer> valMap = new LinkedHashMap<>();
		for (BattleUnity unity : parames.unityList) {
			if (valMap.containsKey(unity.getPlayer())) {
				continue;
			}
			int effvalue = unity.getEffVal(effType());
			valMap.put(unity.getPlayerId(), effvalue);
		}

		Map<String, Integer> val = valMap.entrySet().stream()
				.sorted(((item1, item2) -> {
					int compare = item2.getValue().compareTo(item1.getValue());
					return compare;
				}))
				.filter(ent -> ent.getValue() > 0)
				.limit(ConstProperty.getInstance().getEffect12116Maxinum())
				.collect(Collectors.toMap(e->e.getKey(), e-> e.getValue()));
		return val;
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
