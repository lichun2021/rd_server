package com.hawk.game.battle.effect.impl.eff12131;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 【12134】
- 【万分比】【12134】磁暴干扰：触发荣耀凯恩的【磁暴聚能】效果后，，并降低其 +XX.XX% 的伤害加成（该效果不可叠加，持续 2 回合）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 此作用号绑定作用号【12133】，仅在【12133】的当次追加攻击命中目标后生效
  - 此伤害加成降低效果为debuff效果，记在被攻击方身上
  - 降低伤害加成为外围加成效果；即 敌方实际伤害 = 基础伤害*（1 + 敌方各类伤害加成 - 【本作用值】）
  - 该效果不可叠加，先到先得，持续回合结束后消失
    - 数值不叠加
    - 回合数不可刷新重置
  - 持续回合数读取const表，字段effect12134ContinueRound
    - 注：由被附加开始到当前回合结束，算作 1 回合
 * @author lwt
 * @date 2023年12月4日
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12134)
public class Checker12134 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_2) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
