package com.hawk.game.battle.effect.impl.manhattan;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 【12451】
- 【万分比】【12451】进攻其他指挥官基地战斗时，自身战损每达到 XX.XX%， 造成伤害额外 +XX.XX%（该效果可叠加，至多 X 层）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 该作用号仅在进攻战斗时生效（包含个人进攻和集结进攻）
    - 集结时，进攻方均可生效此作用号，相互独立
  - 该作用号仅在进攻其他玩家基地时生效
    - 注：真实玩家基地才生效
  - 自身战损每达到 XX.XX%
    - 此战损为某战斗单位于战斗中的“死亡”数量，与战斗结束时的实际战损结算完全是两码事
    - 战损比率 = 【100% - 当前数量/战斗初始数量 *100%】
      - 注：真实参与战斗的数量，在战斗开始时判定，即 参谋军威技能、所罗门这种战前改变参战单位数量的机制，对战斗初始数量有影响
      - 注：此为比率数值，最终取值合法区间为【0,100%】
    - 生效此作用号的进攻方，此效果绑定在各不同战斗单位上，各自独立计算
    - 战损阈值数据读取const表，字段effect12451ConditionalRatio
      - 配置格式：万分比
  - 造成伤害额外 +XX.XX%
    - 此伤害加成对于各兵种有单独修正系数（玩家不可见），读取const表，字段effect12451DamageAdjust
      - 配置格式：兵种类型id1_修正系数1,......兵种类型id8_修正系数8
      - 即实际伤害加成 = 【面板数值】 * 修正系数
    - 此伤害加成为额外伤害加成效果，与其他作用号累加计算
      - 即 实际伤害 = 基础伤害*（1 + 其他作用号加成 +【本作用号实际伤害加成】）
  - （该效果可叠加，至多 X 层）
    - 该作用号可叠加，受叠加次数上限限制
      - 层数上限读取const表，字段effect12451Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12451)
public class Checker12451 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (BattleConst.WarEff.CITY_ATK.check(parames.troopEffType)) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
