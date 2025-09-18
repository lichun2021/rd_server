package com.hawk.game.battle.effect.impl.hero1102;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
/**
 * 【12392】
- 【万分比】【12392】任命在战备参谋部时，自身每损失 1%  部队，自身远程单位攻击、超能攻击 +XX.XX%（该效果可叠加，至多 99 层）
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 在战斗每回合判定，满足条件后本轮战斗生效
  - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
  - 自身远程单位攻击，超能攻击 +XX.XX%
    - 远程部队类型包含有：直升机 = 4、狙击兵 = 6、突击步兵 = 5和攻城车 = 7
  - 该作用号为常规外围属性加成效果，与其他作用号累加计算
    - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】）
  - （该效果可叠加，至多 99 层）
    - 效果记录在己方部队身上，数值直接叠加；由于本身具有数值范围，无需额外配置。
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12392)
public class Checker12392  implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (isYuanCheng(parames.type)) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
