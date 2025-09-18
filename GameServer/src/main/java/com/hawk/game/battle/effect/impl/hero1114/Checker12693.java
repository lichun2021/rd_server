package com.hawk.game.battle.effect.impl.hero1114;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 【12693~12694】
- 【万分比】【12693~12694】战技持续期间，锐利同步获得攻击属性增加额外+XX.XX%，坚毅同步获得防御和生命属性增加额外+XX.XX%
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号仅在阿斯缇娅开启战技后，战技持续期间才生效
  - 【12693】绑定锐利同步作用号【12675】，作用号生效时，锐利同步获得攻击属性额外增加，即
    - 即 实际属性 = 基础属性*（1 + 各类加成 +【12675作用值】+【12693作用值】* 自身兵种修正系数/10000）
      - 各固定值沿用作用号【12675】参数
      - 配置格式：万分比
  - 【12694】绑定坚毅同步作用号【12676】，作用号生效时，坚毅同步获得防御和血量属性额外增加，即
    - 即 实际属性 = 基础属性*（1 + 各类加成 +【12676作用值】+【12694作用值】* 自身兵种修正系数/10000）
      - 各固定值沿用作用号【12676】参数
      - 配置格式：万分比
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL3)
@EffectChecker(effType = EffType.HERO_12693)
public class Checker12693 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12681) <= 0) {
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
