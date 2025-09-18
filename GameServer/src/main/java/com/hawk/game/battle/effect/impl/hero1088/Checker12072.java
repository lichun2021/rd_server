package com.hawk.game.battle.effect.impl.hero1088;

import com.hawk.game.battle.effect.BattleConst.WarEff;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.EFF_12072)
public class Checker12072 implements IChecker {
	/**
	 *【12072】
- 【万分比】【12072】战技持续期间，【轻装简行】效果变更为: 降低自身【智能护盾】发动概率 +XX.XX%
  - 该作用号生效时，变更作用号【12053】的数值为本作用值
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 此为英雄战技专属作用号，配置格式如下
    - 作用号id_参数1
      - 参数1：作用值系数
        - 配置格式：浮点数
        - 即本作用值 = 英雄军事值 * 参数1/10000
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.troopEffType != WarEff.NO_EFF && parames.solider.getEffVal(EffType.EFF_12053) > 0) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
	
	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
