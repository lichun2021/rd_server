package com.hawk.game.battle.effect.impl.hero1088;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
【12063~12065】
- 【万分比】【12063】集结战斗开始前，若自身出征直升机数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的直升机单位获得如下效果: 【黑鹰轰炸】命中目标后，额外向敌方远程随机 2 个单位进行 1 轮散射攻击（伤害率XX.XX%，更高概率选择步兵单位），并降低其 +XX.XX% 的攻击加成（该效果可叠加，至多 XX%）；（多个新英雄同时存在时，至多有 2 个直升机单位生效）
- 【万分比】【12064】集结战斗开始前，若自身出征直升机数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的直升机单位获得如下效果: 【黑鹰轰炸】命中目标后，额外向敌方远程随机 2 个单位进行 1 轮散射攻击（伤害率XX.XX%，更高概率选择步兵单位），并降低其 +XX.XX% 的防御加成（该效果可叠加，至多 XX%）；（多个新英雄同时存在时，至多有 2 个直升机单位生效）
- 【万分比】【12065】集结战斗开始前，若自身出征直升机数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的直升机单位获得如下效果: 【黑鹰轰炸】命中目标后，额外向敌方远程随机 2 个单位进行 1 轮散射攻击（伤害率XX.XX%，更高概率选择步兵单位），并降低其 +XX.XX% 的生命加成（该效果可叠加，至多 XX%）；（多个新英雄同时存在时，至多有 2 个直升机单位生效）
  - 上述3个作用号机制完全一致，加的属性类别不一样，分3个作用号开发；直升机兵种类型 = 4
  - 上述3个作用号为作用号【12062】触发的散射攻击命中敌方后的额外效果
    - 注：黑鹰轰炸本身的目标不受此作用号影响，受到额外随机的目标单位才受此作用号影响
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 降低敌方加成为外围属性减少效果；即 敌方部队实际攻击/防御/生命 = 基础属性*（1 + 敌方各类属性加成 - 本作用号属性加成降低）
  - 该效果由被攻击附加后，持续至本场战斗结束
  - 集结中存在多个此作用号时，可以叠加
  - 效果记录在敌方部队身上，数值直接叠加；限制该作用号数值上限
    - 数值上限读取const表，字段effect12063MaxValue、effect12064MaxValue、effect12065MaxValue
      - 配置格式：万分比
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12063)
public class Checker12063 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.type != SoldierType.PLANE_SOLDIER_4 || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}

		return new CheckerKVResult(parames.unity.getEffVal(effType()), 0);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
