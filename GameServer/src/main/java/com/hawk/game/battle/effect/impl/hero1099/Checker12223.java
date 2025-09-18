package com.hawk.game.battle.effect.impl.hero1099;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = { Type.SOLDIER_SKILL })
@EffectChecker(effType = EffType.HERO_12223)
public class Checker12223 implements IChecker {
	/**
	 * - 【万分比】【12223】任命在战备参谋部时，己方任意单位每攻击命中 1 次敌方单位后，自身所有单位超能攻击 +XX.XX%（该效果可叠加，至多 100 层）
	- 战报相关
	- 于战报中展示
	- 合并至精简战报中
	- 在战斗中随时变化，达到层数上限后不再变化
	- 己方任意单位每攻击命中 1 次敌方单位后
	- 己方：若有盟友部队，盟友部队的攻击也算在内
	- 任意单位类型包含有：兵种（1~8）
	- 若为范围攻击，则按攻击命中的敌方单位实际个数进行计算
	- 敌方单位包含有：兵种（1~8）
	- 自身所有单位超能攻击 +XX.XX%
	- 所有单位包含有：兵种（1~8）
	- 该作用号为常规外围属性加成效果，与其他作用号累加计算
	- 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】）
	- （该效果可叠加，至多 100 层）
	- 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
	  - 层数上限读取const表，字段effect12223Maxinum
	    - 配置格式：绝对值
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type)) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
