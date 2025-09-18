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
@EffectChecker(effType = EffType.EFF_12071)
public class Checker12071 implements IChecker {
	/**
	 *【12071】
	- 【万分比】【12071】战技持续期间，【轮番轰炸】命中步兵（攻城车 = 7）单位时，伤害额外 +XX.XX%
  - 该作用号为作用号【轮番轰炸】【12051】生效时的额外效果
  - 此伤害加成为额外伤害加成，与其他各类伤害加成累加计算；即实际伤害 = 基础伤害*（1 + 各类伤害加成 + 【本作用值】）
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 此为英雄战技专属作用号，配置格式如下
    - 作用号id_参数1_参数2
      - 参数1：作用号系数
        - 配置格式：浮点数
        - 即本作用值 = 英雄军事值 * 参数1/10000
      - 参数2：战技持续时间
        - 配置格式：绝对值（单位：秒）
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.troopEffType != WarEff.NO_EFF && parames.solider.getEffVal(EffType.EFF_12051) > 0) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
	
	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
