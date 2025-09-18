package com.hawk.game.battle.effect.impl.hero1099;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = { Type.HP, Type.DEF })
@EffectChecker(effType = EffType.HERO_12221)
public class Checker12221 implements IChecker {
	/**
	 * - 【万分比】【12221】任命在战备参谋部时，敌方出征远程部队数量每达到 20万，自身近战单位防御、生命 +XX.XX%（该效果可叠加，至多 30 层）
	- 战报相关
	- 于战报中展示
	- 合并至精简战报中
	- 在战斗开始前判定，满足条件后本次战斗全程生效且数值不再变化
	- 敌方出征远程部队数量每达到 20万
	- 远程部队类型包含有：直升机 = 4、狙击兵 = 6、突击步兵 = 5和攻城车 = 7
	- 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
	  - 指定数值读取const表，字段effect12221NumLimit
	    - 配置格式：万分比
	- 自身近战单位防御、生命 +XX.XX%
	- 近战部队类型包含有：主战坦克 = 2、防御坦克 = 1、轰炸机 = 3和采矿车 = 8
	- 该作用号为常规外围属性加成效果，与其他作用号累加计算
	- 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】）
	- （该效果可叠加，至多 30 层）
	- 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
	  - 层数上限读取const表，字段effect12221Maxinum
	    - 配置格式：绝对值
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (isJinzhan(parames.type)) {
			int cen = (int) (parames.tarStatic.getTotalCountMarch() / ConstProperty.getInstance().getEffect12221NumLimit());
			cen = Math.min(cen, ConstProperty.getInstance().getEffect12221Maxinum());
			effPer = parames.unity.getEffVal(effType()) * cen;
		}
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
