package com.hawk.game.battle.effect.impl.xqhx;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.module.lianmengXianquhx.XQHXConst.XQHXState;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;

/*
 * 0731新增 【10076】
【万分比】【10076】于先驱回响的核心建筑内战斗时，部队受到伤害减少 xx.XX%【10076】·于先驱回响的核心建筑内战斗时
核心建筑指此副本内，建筑类型为核心建筑的(下方五角星标识建筑)，后续会有配置表标记核心建筑的类0型id，以id作为识别
。当于此建筑内防守，或者进攻此建筑时，获得属性加成。生效逻辑和效果参考作用号10057
该作用号为伤害减少效果，与其他作用号累乘计算，即
实际伤害= 基础伤害*(1-其他作用号)*(1-【本作用值】*敌方兵种修正系数/10000*叠加数)。实际针对敌方各兵种类型，单独配置系数;敌方兵种修正系数 读取const表，字段effect10076SoldierAdjust
·配置格式:兵种类型id1_修正系数1，…….兵种类型id8_修正系数8。修正系数具体配置为万分比
 */
@BattleTupleType(tuple = { Type.REDUCE_HURT_PCT })
@EffectChecker(effType = EffType.XQHX_10076)
public class Checker10076 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.unity.getPlayer().getXQHXState() == XQHXState.GAMEING && isSoldier(parames.type)) {
			effPer = (int) (parames.unity.getEffVal(effType()) * GsConst.EFF_PER* ConstProperty.getInstance().effect10076SoldierAdjustMap.getOrDefault(parames.tarType, 10000));
		}

		return new CheckerKVResult(effPer, effNum);
	}
	
	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
