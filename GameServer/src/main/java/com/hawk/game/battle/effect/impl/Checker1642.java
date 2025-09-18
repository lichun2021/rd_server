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

@BattleTupleType(tuple = Type.ATK)
@EffectChecker(effType = EffType.HERO_1642)
public class Checker1642 implements IChecker {

	// 振军：集结战斗时，如果自身攻城车超过部队总数的 5 % ，全部远程友军攻击+10.44% ，若集结中单排采矿车数量大于自身派遣攻城车数量的30%，此效果翻倍（多个希克斯同时存在，改效果最多可叠加 40%）
	// 触发逻辑：
	// 集结作战时，自身攻城车（兵种类型 =7）士兵数量>= 部队总量 的X% X由const.xml effect1642Per控制 填500 = 5%
	//
	// 额外触发逻辑：
	// 判断集结内，有没有单排攻城车（兵种类型 =8）士兵数量>= 玩家自身攻城车士兵数量*X%，effect1642SoldierPer控制 填3000= 30% 若有，【1642】效果逻辑翻倍
	//
	// 效果逻辑：
	// 集结中所有近战部队（兵种类型 =4，5，6，7）防御增加 ,多个玩家携带【1642】可叠加
	//
	//
	// 目标的防御加成 = 1+其他攻击加成+ Min{【1642】,X}
	//
	// 最大可叠加X%攻击加成，X由const.xml effect1642Maxinum控制 填4000 40%

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		boolean isMass = BattleConst.WarEff.ATK_MASS.check(parames.troopEffType) || BattleConst.WarEff.DEF_MASS.check(parames.troopEffType);
		boolean bfalse = parames.type == SoldierType.PLANE_SOLDIER_4
				|| parames.type == SoldierType.FOOT_SOLDIER_5
				|| parames.type == SoldierType.FOOT_SOLDIER_6
				|| parames.type == SoldierType.CANNON_SOLDIER_7;
		if (bfalse && isMass) {
			Object object = parames.getLeaderExtryParam(getSimpleName());
			if (Objects.nonNull(object)) {
				effPer = (int) object;
			} else {
				List<String> playerIdList = parames.getAllPlayer();
				ArmyInfo max8 = parames.getMaxFreeArmy(SoldierType.CANNON_SOLDIER_8);
				int max8Cnt = Objects.isNull(max8) ? 0 : max8.getFreeCnt();

				double per = ConstProperty.getInstance().getEffect1642Per() * GsConst.EFF_PER;
				double per2 = ConstProperty.getInstance().getEffect1642SoldierPer() * GsConst.EFF_PER;
				for (String playerId : playerIdList) {
					double footCount = parames.getPlayerArmyCount(playerId, SoldierType.CANNON_SOLDIER_7);
					if (footCount / parames.totalCount >= per) {
						int effVal = parames.getEffVal(playerId, effType());
						if (max8Cnt / footCount >= per2) {
							effVal *= 2;
						}
						effPer += effVal;
					}

				}

				effPer = Math.min(effPer, ConstProperty.getInstance().getEffect1642Maxinum());

				parames.putLeaderExtryParam(getSimpleName(), effPer);
			}
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
