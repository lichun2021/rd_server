package com.hawk.game.battle.effect.impl;

import java.util.Optional;

import com.hawk.game.battle.BattleSoldier_2;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.hero.skill.IHeroSkill;
import com.hawk.game.player.hero.skill.Skill1078;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_1658)
public class Checker1658 implements IChecker {

	/**
	 * 稀有军事芯片 
	
	技能描述
	出征或驻防时，当步兵受到巨大伤害时，有概率医疗本次攻击xx.xx%受伤的士兵，继续参与战斗。
	
	作用号需求1658
	出征或驻防时，士兵类型 = 5 或 6 的士兵的单排目标，当受到单次攻击，造成其损失数量的大于等于 1%的时候，即 本次损失数量/战斗开始之前本排士兵数量 >= 1%（除数包含所罗门影子冰） ，有概率减低，本次损失数量降低【1658】
	
	【1658】为万分比数值
	实际损失数 = 本次损失数 * （1-【1658】）
	
	触发概率常规 读取const.xml  effect1658Per
	填800 即触发概率为8%
	
	当战斗中，士兵类型 = 5 或 6 的士兵的单排目标，当受到单次攻击为昆娜作用号【1653】致死伤害，且造成其损失数量的大于等于 1%的时候，触发概率为特殊增强概率
	
	触发概率特殊 读取const.xml  effect1658Per2
	填4000 即触发概率为40% 
	
	测试注意，玩家出征两个英雄可携带双芯片，两者【1658】效果值损失数量降低可叠加，但触发概率不可叠加
	
	此芯片只有出征或驻防时，两个军事英雄携带有效
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_5 || parames.type == SoldierType.FOOT_SOLDIER_6) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
