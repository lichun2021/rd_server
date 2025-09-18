package com.hawk.game.battle.effect.impl.eff12131;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.battle.effect.BattleConst.WarEff;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 【12141】
	- 【万分比】【12141】战技持续期间，主战坦克受到攻击时，伤害减少 +XX.XX%
	  - 战报相关
	- 于战报中展示
	- 不合并至精简战报中
	  - 此伤害减少效果算式与其他伤害减少效果累乘计算；即实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 己方某伤害减少）*（1 - 本作用值伤害减少）
	  - 此为英雄战技专属作用号，配置格式如下
	- 作用号id_参数1_参数2
	  - 参数1：作用号系数
	    - 配置格式：浮点数
	    - 即本作用值 = 英雄军事值 * 参数1/10000
	  - 参数2：战技持续时间
	    - 配置格式：绝对值（单位：秒）
 * @author lwt
 * @date 2023年12月4日
 */

@BattleTupleType(tuple = Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.HERO_12141)
public class Checker12141 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_2 && parames.troopEffType != WarEff.NO_EFF) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
