package com.hawk.game.battle.effect.impl;

import java.util.Objects;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_1643)
public class Checker1643 implements IChecker {

	// 作用号逻辑：
	//
	// 【1643】
	//
	// 触发逻辑：自身轰炸机（兵种类型 =3）在每次触发 【士兵技能：battle_soldier_skill_303】和 【专属行芯片聚能反击作用号：1543】后赋予效果，持续至战斗结束。
	//
	//
	//
	// 额外触发逻辑：
	//
	// 判断个人或集结内，有没有单排直升机（兵种类型 =4）士兵数量>= 玩家自身轰炸机士兵数量*X%，effect1643SoldierPer控制 填5000= 50%，若有，【1643】效果逻辑翻倍
	//
	// 注意无需每回合都要判断战斗开始满足即可，享受永久双倍速叠加
	//
	// 效果逻辑：
	// 轰炸机攻击增加，每次满足触发条件，均可触发堆叠，堆叠有上限
	// 轰炸机攻击加成 = 1+其他攻击加成+ Min{【1643】,X}
	//
	// 最大可叠加X%攻击加成，X由const.xml effect1643Maxinum控制 填10000  即100%
	//
	// 【1643】是万分比参数 

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.PLANE_SOLDIER_3) {
			effPer = parames.unity.getEffVal(effType());
			if (effPer > 0) {
				ArmyInfo max8 = parames.getMaxFreeArmy(SoldierType.PLANE_SOLDIER_4);
				int max8Cnt = Objects.isNull(max8) ? 0 : max8.getFreeCnt();
				double footCount = parames.getPlayerArmyCount(parames.unity.getPlayer().getId(), SoldierType.PLANE_SOLDIER_3);
				double per = ConstProperty.getInstance().getEffect1643SoldierPer() * GsConst.EFF_PER;
				if (max8Cnt >= footCount * per) {
					effPer = effPer * 2;
				}
			}

		}

		return new CheckerKVResult(effPer, effNum);
	}
}
