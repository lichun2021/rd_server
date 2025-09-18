package com.hawk.game.battle.effect.impl.hero1098;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * - - 【万分比】【12207~12208】磁能环护：出征或驻防时，自身出征数量最多的突击步兵受到空军单位攻击时，伤害减少 【XX.XX% + 敌方损坏单位数*XX.XX%】（敌方处于【损坏状态】的单位计算时至多取 10 个）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 自身出征数量最多的突击步兵受到空军单位攻击时
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的突击步兵（兵种类型 = 5）生效（若存在多个突击步兵数量一样且最高，取等级高的）
    - 空军单位包含有：直升机 = 4和轰炸机 = 3
  - 该作用号为伤害减少效果，与其他作用号累乘计算，即 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【12207】 - 【12208】）
    - 【12208】实际数值 = 敌方当前处于【损坏状态】的单位数*英雄系数
      - 敌方单位数：敌方战报中的单排计数
        - 不同玩家不同等级的部队（兵种类型 = 1~8）均视为独立单位
        - 单人时取敌方单人所有部队，集结时取集结敌方所有玩家的所有部队
          - 注：敌方处于【损坏状态】的幻影部队，不计入这里的计算
      - 另外该计算对于敌方单位数量有上限限制，读取const表，字段effect12208MaxNum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12207)
public class Checker12207 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12201) == 0) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = parames.unity.getEffVal(effType());
		int effNum = 0;

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
