package com.hawk.game.battle.effect.impl;

import java.util.List;
import java.util.Objects;

import com.hawk.game.battle.effect.BattleConst;
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

@BattleTupleType(tuple = Type.HP)
@EffectChecker(effType = EffType.HERO_1641)
public class Checker1641 implements IChecker {

	// 【1641】
	// 触发逻辑：
	// 集结作战时，自身采矿车（兵种类型 =8）士兵数量>= 部队总量 的X% X由const.xml effect1641Per控制 填500 = 5%
	//
	// 额外触发逻辑：
	// 判断集结内，有没有单排攻城车（兵种类型 =7）士兵数量>= 玩家自身采矿车士兵数量，若有，【1641】效果逻辑翻倍
	//
	// 效果逻辑：
	// 集结中所有近战部队（兵种类型 =1，2，3，8）防御增加 ,多个玩家携带【1641】可叠加
	//
	//
	// 目标的防御加成 = 1+其他防御加成+ Min{【1641】,X}
	//
	// 最大可叠加X%防御加成，X由const.xml effect1641Maxinum控制 填4000 40%
	//
	// 【1641】是万分比参数

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		boolean isMass = BattleConst.WarEff.ATK_MASS.check(parames.troopEffType) || BattleConst.WarEff.DEF_MASS.check(parames.troopEffType);
		boolean bfalse = parames.type == SoldierType.TANK_SOLDIER_1
				|| parames.type == SoldierType.TANK_SOLDIER_2
				|| parames.type == SoldierType.PLANE_SOLDIER_3
				|| parames.type == SoldierType.CANNON_SOLDIER_8;
		if (bfalse && isMass) {
			Object object = parames.getLeaderExtryParam(getSimpleName());
			if (Objects.nonNull(object)) {
				effPer = (int) object;
			} else {
				List<String> playerIdList = parames.getAllPlayer();
				ArmyInfo max7 = parames.getMaxFreeArmy(SoldierType.CANNON_SOLDIER_7);
				int max7Cnt = Objects.isNull(max7) ? 0 : max7.getFreeCnt();

				double per = ConstProperty.getInstance().getEffect1641Per() * GsConst.EFF_PER;
				for (String playerId : playerIdList) {
					double footCount = parames.getPlayerArmyCount(playerId, SoldierType.CANNON_SOLDIER_8);
					if (footCount / parames.totalCount >= per) {
						int effVal = parames.getEffVal(playerId, effType());
						if (max7Cnt >= footCount) {
							effVal *= 2;
						}
						effPer += effVal;
					}

				}

				effPer = Math.min(effPer, ConstProperty.getInstance().getEffect1641Maxinum());

				parames.putLeaderExtryParam(getSimpleName(), effPer);
			}
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
