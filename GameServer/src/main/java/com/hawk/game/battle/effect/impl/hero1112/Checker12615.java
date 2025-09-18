package com.hawk.game.battle.effect.impl.hero1112;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * - 磁锋力场：战斗开始时，使自身突击步兵部署磁力装置，于战场中形成磁锋力场，自身出征数量最多的突击步兵获得下述效果：
- 【万分比】【12615】磁流强化：每X(effect12615AtkRound) 回合开始时，自身攻击、防御、生命增加固定值(effect12615BaseVaule)+XX.XX%【12615】（该效果可叠加，至多X(effect12615Maxinum) 层）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 每第 X 回合开始时
    - 战斗中每第 X 的倍数的回合开始后，自身开始普攻前，进行判定
    - 指定回合数读取const表，字段effect12615AtkRound
      - 配置格式：绝对值
  - 自身攻击、防御、生命增加固定值+XX.XX%
    - 增加属性为外围属性增加效果；
      - 即 敌方部队实际属性 = 基础属性*（1 + 属性加成 + 【本作用号值】）
    - 攻击、防御、血量增加固定数值读取const表，字段effect12615BaseVaule
      - 配置格式：万分比
    - 攻击、防御、血量增加作用值读取作用号【12615】
      - 配置格式：万分比
  - （该效果可叠加，至多X(effect12615Maxinum) 层）
    - 层数上限读取const表，字段effect12615Maxinum
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12615)
public class Checker12615 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity.getEffVal(EffType.HERO_12611) <= 0) {
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
