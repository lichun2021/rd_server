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
 【12142】
	- 【万分比】【12142】战技持续期间，触发磁暴干扰时，暴击几率 +XX.XX%，
	  - 战报相关
	- 于战报中隐藏
	- 不合并至精简战报中
	  - 此作用号绑定磁暴干扰作用号【12133】，仅在【12133】的当次追加攻击命中目标时生效
	  - 此为英雄战技专属作用号，配置格式如下：
	- 作用号id_参数1
	  - 参数1：作用号系数
	    - 配置格式：浮点数
	    - 即本作用值 = 英雄军事值 * 参数1/10000
 * @author lwt
 * @date 2023年12月4日
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12142)
public class Checker12142 implements IChecker {
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
