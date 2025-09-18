package com.hawk.game.battle.effect.impl;

import java.util.Optional;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

/**
 * 【1650】
	集结环境下
	
	效果为 ：士兵类型 =1 防御坦克 死转伤 比例额外增加
	
	若 泰能防御坦克数量 即 士兵类型 = 1  中的battle_soldier level  14 士兵，占士兵类型 1 士兵数量的 y% 以上（大于关系）
	y 由const.xml 的 effect1650Per控制， 填5000，即50%
	死转伤比例 则翻倍
	
	最终防御坦克阵亡数 = 基础阵亡数*（1-【1650】）*其他损失比例
	
	【1651】
	集结环境下
	
	效果为 ：士兵类型 =2 主战坦克 暴击伤害额外增加
	若 泰能主战坦克数量 即 士兵类型 = 2  中的battle_soldier level  13 士兵，占士兵类型 2 士兵数量的 y% 以上（大于关系）
	y 由const.xml 的 effect1651Per控制， 填5000，即50%
	暴击伤害  则翻倍
	暴击伤害 = 基础伤害 *（1+【1651】+其他暴击伤害比例）
 * @author lwt
 * @date 2022年8月3日
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_1651)
public class Checker1651 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (BattleConst.WarEff.MASS.check(parames.troopEffType) && parames.type == SoldierType.TANK_SOLDIER_2) {
			effPer = parames.unity.getEffVal(effType());
			if (effPer > 0) {
				String playerId = parames.unity.getPlayer().getId();
				double taotal1 = parames.getPlayerArmyCount(playerId, SoldierType.TANK_SOLDIER_2);
				double taotalTai1 = Optional.ofNullable(parames.unitStatic.getPlantUnityStatistics().getPlayerSoldierCount().get(playerId, SoldierType.TANK_SOLDIER_2)).orElse(0);
				if (taotalTai1 / taotal1 > ConstProperty.getInstance().getEffect1651Per() * GsConst.EFF_PER) {
					effPer = effPer * 2;
				}
			}
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
