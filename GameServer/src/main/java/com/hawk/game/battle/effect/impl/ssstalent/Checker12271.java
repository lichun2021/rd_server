package com.hawk.game.battle.effect.impl.ssstalent;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 
12271】
- 【万分比】【12271】轮番轰炸攻击轮次，在集结时由 18 轮增至 20 轮；在个人战时由 9 轮增至 12 轮
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号机制是将伊娜莉莎的专属作用号【12051】的攻击轮次进行变更
    - 其作用号本身数值无意义，不为0则生效此作用号效果
  - 基础攻击轮次随战斗类型有所区别
    - 集结战斗时（包含集结进攻和集结防守，存在友军玩家即为生效）读取const表，字段effect12271AtkTimesForMass
      - 配置格式：绝对值
    - 个人战斗时（包含个人进攻和个人防守，只有自身1个玩家即为生效）读取const表，字段effect12271AtkTimesForPerson
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.EFF_12271)
public class Checker12271 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.EFF_12051) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		effPer = parames.unity.getEffVal(effType());
		return new CheckerKVResult(effPer, effNum);
	}
}
