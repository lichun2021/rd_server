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
@EffectChecker(effType = EffType.HERO_1644)
public class Checker1644 implements IChecker {

	// 【1644】
	//
	// 触发逻辑：自身直升机（兵种类型 =4）在每次触发 【士兵技能：battle_soldier_skill_304】和 【专属行芯片地狱火轰炸作用号：1533】后赋予效果，持续至战斗结束。
	//
	//
	//
	// 额外触发逻辑：
	//
	// 判断个人或集结内，有没有单排轰炸机（兵种类型 =3）士兵数量>= 玩家自身直升机士兵数量*X%，effect1644SoldierPer控制 填5000= 50%，若有，【1644】效果逻辑翻倍
	//
	// 注意无需每回合都要判断战斗开始满足即可，享受永久双倍速叠加
	//
	// 效果逻辑：
	// 直升机攻击增加，每次满足触发条件，均可触发堆叠，堆叠有上限
	// 直升机攻击加成 = 1+其他攻击加成+ Min{【1644】,X}
	//
	// 最大可叠加X%攻击加成，X由const.xml effect1644Maxinum控制 填10000  即100%
	//
	// 【1644】是万分比参数 

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.PLANE_SOLDIER_4) {
			effPer = parames.unity.getEffVal(effType());
			if (effPer > 0) {
				ArmyInfo max8 = parames.getMaxFreeArmy(SoldierType.PLANE_SOLDIER_3);
				int max8Cnt = Objects.isNull(max8) ? 0 : max8.getFreeCnt();
				double footCount = parames.getPlayerArmyCount(parames.unity.getPlayer().getId(), SoldierType.PLANE_SOLDIER_4);
				double per = ConstProperty.getInstance().getEffect1644SoldierPer() * GsConst.EFF_PER;
				if (max8Cnt >= footCount * per) {
					effPer = effPer * 2;
				}
			}

		}

		return new CheckerKVResult(effPer, effNum);
	}
}
