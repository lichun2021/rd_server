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
 * 0731新增【10077】
【万分比】【10077】部队受到伤害减少 XX.XX%【10077】
该作用号为伤害减少效果，与其他作用号累乘计算，即
实际伤害= 基础伤害*(1-其他作用号)*(1-【本作用值】*敌方兵种修正系数/10000*叠加数)0实际针对敌方各兵种类型，单独配置系数;敌方兵种修正系数 读取const表，字段effect10077SoldierAdjust
·配置格式:兵种类型id1_修正系数1，…兵种类型id8_修正系数8修正系数具体配置为万分比
 */
@BattleTupleType(tuple = { Type.REDUCE_HURT_PCT })
@EffectChecker(effType = EffType.XQHX_10077)
public class Checker10077 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.unity.getPlayer().getXQHXState() == XQHXState.GAMEING && isSoldier(parames.type)) {
			effPer = (int) (parames.unity.getEffVal(effType()) * GsConst.EFF_PER* ConstProperty.getInstance().effect10077SoldierAdjustMap.getOrDefault(parames.tarType, 10000));
		}

		return new CheckerKVResult(effPer, effNum);
	}
	
	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
