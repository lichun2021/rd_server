package com.hawk.game.battle.effect.impl.hero1108;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
【12512】
- 关联【12511】：能量激荡状态攻击时额外附加如下效果：
- 【万分比】【12512】洪流战幕：对敌方单位造成 X（effect12512CritTimes） 次暴击后，自身主战坦克受到伤害减少 +XX.XX%【12512】（该效果可以叠加，最多 X（effect12512Maxinum）层）->针对敌方兵种留个内置修正系数（effect12512SoldierAdjust）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 能量激荡状态攻击时额外附加本效果
    - 关联【12511】，与【12511】相同触发条件
  - 对敌方单位造成 X 次暴击后
    - 读取const表，字段effect12512CritTimes
      - 配置格式：绝对值
  - 自身主战坦克受到伤害减少 +XX.XX%（该效果可以叠加，最多 X 层）
    - 该作用号为伤害减少效果，与其他作用号累乘计算，即 
      - 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【本作用值】* 敌方兵种修正系数/10000 * 叠加数）
    - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect12512SoldierAdjust
      - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
        - 修正系数具体配置为万分比
    - 层数上限读取const表，字段effect12512Maxinum
      - 配置格式：绝对值
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12512)
public class Checker12512 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12511) <= 0) {
			return CheckerKVResult.DefaultVal;
		}
		int effPer = 0;
		int effNum = 0;
		effPer = parames.unity.getEffVal(effType());

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}